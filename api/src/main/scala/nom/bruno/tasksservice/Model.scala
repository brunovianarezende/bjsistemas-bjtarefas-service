package nom.bruno.tasksservice

import slick.jdbc.MySQLProfile.api._

object Tables {

  case class Task(id: Option[Int], title: String, description: String)

  class Tasks(tag: Tag) extends Table[Task](tag, "tasks") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def title = column[String]("title", O.Length(128))

    def description = column[String]("description", O.Length(512))

    def * = (id.?, title, description) <> (Task.tupled, Task.unapply)
  }

  val tasks = TableQuery[Tasks]

}
