package nom.bruno.tasksservice.routes

import javax.inject.{Inject, Named}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import nom.bruno.tasksservice.routes.Directives._
import nom.bruno.tasksservice.services.TasksService
import nom.bruno.tasksservice.{ChangeTask, Error}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class TasksRoutes @Inject()(tasksService: TasksService)
                           (@Named("EC") implicit val ec: ExecutionContext) extends BaseRoutes {

  val logger = LoggerFactory.getLogger(getClass)

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
      } ~ put {
        entity(as[ChangeTask]) { taskUpdate =>
          lazy val taskNotFound = complete(404, Fail(Error.TaskDoesntExist))
          Try(taskId.toInt) match {
            case Success(id) => {
              unwrapSuccess(tasksService.validateUpdateTask(id, taskUpdate) flatMap {
                case Some(task) => {
                  tasksService.updateTask(task) map { _ => complete(Ok) }
                }
                case None => Future {
                  taskNotFound
                }
              })
            }
            case _ => taskNotFound
          }
        }
      }
    }
}
