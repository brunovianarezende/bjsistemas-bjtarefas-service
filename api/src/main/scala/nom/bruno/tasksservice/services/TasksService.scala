package nom.bruno.tasksservice.services

import javax.inject.Inject

import nom.bruno.tasksservice.Tables.Task
import nom.bruno.tasksservice.repositories.TaskRepository

import scala.concurrent.Future

class TasksService @Inject()(taskRepository: TaskRepository) {

  def getTasks: Future[Seq[Task]] = {
    taskRepository.getAllTasks
  }
}
