package nom.bruno.tasksservice.services

import javax.inject.{Inject, Named}

import nom.bruno.tasksservice.{TaskCreation, TaskUpdate}
import nom.bruno.tasksservice.Tables.Task
import nom.bruno.tasksservice.repositories.TaskRepository

import scala.concurrent.{ExecutionContext, Future}

class TasksService @Inject()(taskRepository: TaskRepository)
                            (@Named("EC") implicit val executionContext: ExecutionContext) {
  def validateTaskPosition(position: Int): Future[Boolean] = ???

  def moveTask(task: Task, position: Int): Future[Unit] = ???

  def getNumberOfTasks: Future[Int] = ???

  def validateUpdateTask(id: Int, taskUpdate: TaskUpdate): Future[Option[Task]] = {
    taskRepository.getTask(id) map {
      case Some(task) => Some(task.copy(title = taskUpdate.title.getOrElse(task.title),
        description = taskUpdate.description.getOrElse(task.description)))
      case None => None
    }
  }

  def updateTask(task: Task): Future[Unit] = {
    taskRepository.updateTask(task) map (_ => ())
  }

  def addTask(taskData: TaskCreation): Future[Task] = {
    val newTask = Task(None, taskData.title, taskData.description)
    taskRepository.addTask(newTask) map {id =>
      newTask.copy(id=Some(id))
    }
  }

  def getTasks: Future[Seq[Task]] = {
    taskRepository.getAllTasks
  }

  def searchForTasks: Future[Seq[Task]] = {
    taskRepository.getTasks(deleted = false)
  }

  def deleteTask(task: Task): Future[Unit] = {
    taskRepository.deleteTask(task.id.get) map (_ => ())
  }

  def softDeleteTask(task: Task): Future[Unit] = {
    taskRepository.markAsDeleted(task.id.get) map (_ => ())
  }

  def getTask(id: Int): Future[Option[Task]] = {
    taskRepository.getTask(id)
  }
}
