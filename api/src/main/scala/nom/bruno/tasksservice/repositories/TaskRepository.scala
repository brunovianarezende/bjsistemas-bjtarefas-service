package nom.bruno.tasksservice.repositories

import javax.inject.{Inject, Named}

import nom.bruno.tasksservice.Tables.{Task, tasks}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class TaskRepository @Inject()(val db: Database)
                              (@Named("EC") implicit val executionContext: ExecutionContext) {
  def updateTask(taskToUpdate: Task): Future[Int] = {
    val q = for {
      t <- tasks if t.id === taskToUpdate.id
    } yield {
      (t.title, t.description)
    }
    val updateAction = q.update(taskToUpdate.title, taskToUpdate.description)
    db.run(updateAction)
  }

  def getAllTasks: Future[Seq[Task]] = {
    val query = tasks
    db.run(query.result)
  }

  def getTasks(deleted: Boolean = false): Future[Seq[Task]] = {
    val query = tasks.filter(_.deleted === deleted)
    db.run(query.result)
  }

  def getTask(id: Int): Future[Option[Task]] = {
    val query = tasks.filter(_.id === id)
    db.run(query.result.headOption)
  }

  def deleteTask(id: Int): Future[Int] = {
    val query = tasks.filter(_.id === id).delete
    db.run(query)
  }

  def markAsDeleted(id: Int): Future[Int] = {
    val q = for {
      t <- tasks if t.id === id
    } yield {
      (t.deleted)
    }
    val updateAction = q.update(true)
    db.run(updateAction)
  }

  def addTask(task: Task): Future[Int] = {
    db.run((tasks returning tasks.map(_.id)) += task)
  }
}
