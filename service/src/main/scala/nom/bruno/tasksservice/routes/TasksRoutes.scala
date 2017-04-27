package nom.bruno.tasksservice.routes

import javax.inject.{Inject, Named}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import nom.bruno.tasksservice.services.TasksService

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class TasksRoutes @Inject()(tasksService: TasksService)
                           (@Named("EC") implicit val ec: ExecutionContext) extends BaseRoutes {
  override def routes: Route = path("tasks") {
    get {
      onSuccess(tasksService.getTasks) { tasks =>
        complete(Ok(tasks))
      }
    }
  } ~
    path("tasks" / Segment) { taskId =>
      delete {
        Try(taskId.toInt) match {
          case Success(id) => {
            onSuccess(tasksService.getTask(id) map {
              case Some(task) => {
                tasksService.deleteTask(task) map (_ => ())
              }
              case None => ()
            }) { _ =>
              complete(Ok)
            }
          }
          case _ => {
            complete(Ok)
          }
        }
      }
    }
}
