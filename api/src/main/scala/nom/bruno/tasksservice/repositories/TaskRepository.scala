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

  def getTask(id: Int): Future[Option[Task]] = {
    val query = tasks.filter(_.id === id)
    db.run(query.result.headOption)
  }

  def deleteTask(id: Int): Future[Int] = {
    val query = tasks.filter(_.id === id).delete
    db.run(query)
  }
}
