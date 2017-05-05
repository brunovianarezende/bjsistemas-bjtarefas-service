package nom.bruno.tasksservice.routes

import javax.inject.{Inject, Named}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import nom.bruno.tasksservice.routes.Directives._
import nom.bruno.tasksservice.services.TasksService
import nom.bruno.tasksservice.{Error, TaskCreation, TaskUpdate, TaskView}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class TasksRoutes @Inject()(tasksService: TasksService)
                           (@Named("EC") implicit val ec: ExecutionContext) extends BaseRoutes {

  val logger = LoggerFactory.getLogger(getClass)

  override def routes: Route = path("tasks") {
    get {
      onSuccess(tasksService.searchForTasks) { tasks =>
        complete(Ok(tasks.map(TaskView.from(_))))
      }
    } ~ post {
      entity(as[TaskCreation]) { taskData =>
        unwrapSuccess(tasksService.addTask(taskData) map { newTask =>
          complete(Ok(newTask.id.get))
        })
      }
    }
  } ~
    path("tasks" / Segment) { taskId =>
      delete {
        Try(taskId.toInt) match {
          case Success(id) => {
            onSuccess(tasksService.getTask(id) map {
              case Some(task) => {
                tasksService.softDeleteTask(task) map (_ => ())
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
        entity(as[TaskUpdate]) { taskUpdate =>
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
