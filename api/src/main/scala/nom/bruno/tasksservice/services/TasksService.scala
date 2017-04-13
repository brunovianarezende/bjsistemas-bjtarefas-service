package nom.bruno.tasksservice.services

import nom.bruno.tasksservice.Tables.Task

class TasksService {
  def getTasks: Seq[Task] = {
    Seq(
      Task(Some(1), "Task", "Description"),
      Task(Some(2), "Task 2", "Description 2")
    )
  }
}
