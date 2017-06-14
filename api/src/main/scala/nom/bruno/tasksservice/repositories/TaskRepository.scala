package nom.bruno.tasksservice.repositories

import javax.inject.{Inject, Named}

import nom.bruno.tasksservice.Tables.{Task, tasks}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}

trait TaskRepository {
  def getPriorityForTaskAndPrevious(reference: Task): Future[(Int, Int)] = ???

  def getPriorityForTaskAndNext(reference: Task): Future[(Int, Int)] = ???

  def reassignPriorities(): Future[Int] = ???

  def updateTaskPriority(task: Task, newPriority: Int): Future[Int] = ???

  def updateTask(taskToUpdate: Task): Future[Int]

  def getAllTasks: Future[Seq[Task]]

  def getTasks(deleted: Boolean = false): Future[Seq[Task]]

  def getTask(id: Int): Future[Option[Task]]

  def deleteTask(id: Int): Future[Int]

  def markAsDeleted(id: Int): Future[Int]

  def addTask(task: Task): Future[Int]
}

class TaskRepositoryDb @Inject()(val db: Database)
                                (@Named("EC") implicit val executionContext: ExecutionContext) extends TaskRepository {
  override def updateTask(taskToUpdate: Task): Future[Int] = {
    val q = for {
      t <- tasks if t.id === taskToUpdate.id
    } yield {
      (t.title, t.description)
    }
    val updateAction = q.update(taskToUpdate.title, taskToUpdate.description)
    db.run(updateAction)
  }

  override def getAllTasks: Future[Seq[Task]] = {
    val query = tasks
    db.run(query.result)
  }

  override def getTasks(deleted: Boolean = false): Future[Seq[Task]] = {
    val query = tasks.filter(_.deleted === deleted)
    db.run(query.result)
  }

  override def getTask(id: Int): Future[Option[Task]] = {
    val query = tasks.filter(_.id === id)
    db.run(query.result.headOption)
  }

  override def deleteTask(id: Int): Future[Int] = {
    val query = tasks.filter(_.id === id).delete
    db.run(query)
  }

  override def markAsDeleted(id: Int): Future[Int] = {
    val q = for {
      t <- tasks if t.id === id
    } yield {
      t.deleted
    }
    val updateAction = q.update(true)
    db.run(updateAction)
  }

  override def addTask(task: Task): Future[Int] = {
    db.run((tasks returning tasks.map(_.id)) += task)
  }
}

// scalaWart -> overcome the fact that scala classes can't have only implicit parameter lists
class TaskRepositoryStub(scalaWart: Boolean = true)(implicit val executionContext: ExecutionContext) extends TaskRepository {

  class Record(var task: Task, var deleted: Boolean, var priority: Int)

  private var nextId = 1
  private var smallerPriority = 0

  private val records: ListBuffer[Record] = new ListBuffer[Record]

  private def findRecordIndex(taskId: Int): Int = {
    records.indexWhere(r => r.task.id.contains(taskId))
  }

  private def updateRecord(taskId: Int, f: (Record => Any)): Future[Int] = {
    findRecordIndex(taskId) match {
      case -1 => Future {
        0
      }
      case position =>
        f(records(position))
        Future {
          1
        }
    }
  }

  override def updateTask(taskToUpdate: Task): Future[Int] = {
    updateRecord(taskToUpdate.id.get, _.task = taskToUpdate)
  }

  override def getAllTasks: Future[Seq[Task]] = {
    Future {
      records.map(_.task)
    }
  }

  override def getTasks(deleted: Boolean): Future[Seq[Task]] = {
    Future {
      records.filter(_.deleted == deleted).sortBy(_.priority).map(_.task)
    }
  }

  override def getTask(id: Int): Future[Option[Task]] = {
    Future {
      records.map(_.task).find(_.id == id)
    }
  }

  override def deleteTask(id: Int): Future[Int] = {
    findRecordIndex(id) match {
      case -1 => Future {
        0
      }
      case position =>
        records.remove(position)
        Future {
          1
        }
    }
  }

  override def markAsDeleted(id: Int): Future[Int] = {
    updateRecord(id, _.deleted = true)
  }

  override def addTask(task: Task): Future[Int] = {
    val newTask = task.copy(id = Some(nextId))
    smallerPriority -= 1000
    records.insert(0, new Record(newTask, false, smallerPriority))
    nextId += 1
    Future {
      newTask.id.get
    }
  }

  override def getPriorityForTaskAndPrevious(reference: Task): Future[(Int, Int)] = {
    val index = findRecordIndex(reference.id.get)
    val myPriority = records(index).priority
    Future {
      (myPriority, records.map(_.priority).filter(_ < myPriority).reduceOption(Math.max).getOrElse(myPriority - 1000))
    }
  }

  override def getPriorityForTaskAndNext(reference: Task): Future[(Int, Int)] = {
    val index = findRecordIndex(reference.id.get)
    val myPriority = records(index).priority
    Future {
      (myPriority, records.map(_.priority).filter(_ > myPriority).reduceOption(Math.min).getOrElse(myPriority + 1000))
    }
  }

  override def reassignPriorities(): Future[Int] = {
    val interval = Int.MaxValue / records.size

    var nextPriority = 0
    records.foreach(r => {
      r.priority = nextPriority
      nextPriority += interval
    })
    Future {
      records.size
    }
  }

  override def updateTaskPriority(task: Task, newPriority: Int): Future[Int] = {
    updateRecord(task.id.get, r => r.priority = newPriority)
  }

  private def initRecords(tasks: Seq[Task]): Unit = {
    tasks.reverse.foreach(t => {
      smallerPriority -= 1000
      records.append(new Record(t, false, smallerPriority))
    })
    nextId = records.map(_.task.id.get).max + 1
  }
}

object TaskRepositoryStub {
  def apply(tasks: Seq[Task])(implicit executionContext: ExecutionContext): TaskRepositoryStub = {
    val result = new TaskRepositoryStub()
    result.initRecords(tasks)
    result
  }
}