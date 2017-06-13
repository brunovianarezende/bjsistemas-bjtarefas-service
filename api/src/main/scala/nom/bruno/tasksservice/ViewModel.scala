package nom.bruno.tasksservice

import nom.bruno.tasksservice.Tables.Task

case class Error(code: Int, description: String)

object Error {
  val TaskDoesntExist = Error(1, "Task doesn't exist")
  val InvalidPosition = Error(10, "Invalid position")
  val BadSchema = Error(400, "Bad Schema")
}

case class Result[T](success: Boolean, data: Option[T], errors: Option[List[Error]])

case class TaskView(id: Int, title: String, description: String)

case class TaskUpdate(title: Option[String], description: Option[String])

case class TaskCreation(title: String, description: String)

object TaskView {
  def from(task: Task): TaskView = {
    TaskView(task.id.get, task.title, task.description)
  }
}