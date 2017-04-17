package nom.bruno.tasksservice.services

import javax.inject.{Inject, Named}

import nom.bruno.tasksservice.Tables.Task
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.{ExecutionContext, Future}

class TaskRepository @Inject()(val db: Database)
                              (@Named("EC") implicit val executionContext: ExecutionContext) {
  def getAllTasks: Future[Seq[Task]] = Future {
    Seq.empty[Task]
  }
}
