package nom.bruno.tasksservice

case class Error(code: Int, description: String)

object Error {
  val TaskDoesntExist = Error(1, "Task doesn't exist")
  val BadSchema = Error(400, "Bad Schema")
}

case class Result[T](success: Boolean, data: Option[T], errors: Option[List[Error]])

case class ChangeTask(title: Option[String], description: Option[String])