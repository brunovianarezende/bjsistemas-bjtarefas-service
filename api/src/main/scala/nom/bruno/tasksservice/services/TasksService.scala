package nom.bruno.tasksservice.services

import javax.inject.{Inject, Named}

import nom.bruno.tasksservice.Tables.Task
import nom.bruno.tasksservice.repositories.TaskRepository

import scala.concurrent.{ExecutionContext, Future}

class TasksService @Inject()(taskRepository: TaskRepository)
                            (@Named("EC") implicit val executionContext: ExecutionContext) {

  def getTasks: Future[Seq[Task]] = {
    taskRepository.getAllTasks
  }

  def deleteTask(task: Task): Future[Unit] = {
    taskRepository.deleteTask(task.id.get) map (_ => ())
  }

  def getTask(id: Int): Future[Option[Task]] = {
    taskRepository.getTask(id)
  }
}
