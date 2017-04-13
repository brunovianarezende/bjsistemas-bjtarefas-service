package nom.bruno.tasksservice

object Tables {

  case class Task(id: Option[Int], title: String, description: String)

}
