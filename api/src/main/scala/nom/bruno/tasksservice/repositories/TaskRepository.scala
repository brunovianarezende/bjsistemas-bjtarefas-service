package nom.bruno.tasksservice.repositories

import javax.inject.{Inject, Named}

import nom.bruno.tasksservice.Tables.{Task, tasks}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class TaskRepository @Inject()(val db: Database)
                              (@Named("EC") implicit val executionContext: ExecutionContext) {
  def getAllTasks: Future[Seq[Task]] = {
    val query = tasks
    db.run(query.result)
  }
}
