package nom.bruno.tasksservice.routes

import javax.inject.{Inject, Named}

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import nom.bruno.tasksservice.Tables.Task
import nom.bruno.tasksservice.routes.Directives._
import nom.bruno.tasksservice.services.TasksService
import nom.bruno.tasksservice.{Error, Tables, TaskCreation, TaskUpdate, TaskView}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class TasksRoutes @Inject()(tasksService: TasksService)
                           (@Named("EC") implicit val ec: ExecutionContext) extends BaseRoutes {
  override def routes: Route = path("tasks") {
    get {
      onSuccess(tasksService.searchForTasks) { tasks =>
        complete(Ok(tasks.map(TaskView.from)))
      }
    } ~ post {
      entity(as[TaskCreation]) { taskData =>
        onSuccessUnwrap(tasksService.addTask(taskData) map { newTask =>
          complete(Ok(newTask.id.get))
        })
      }
    }
  } ~
    path("tasks" / Segment) { taskId =>
      delete {
        Try(taskId.toInt) match {
          case Success(id) =>
            onSuccess(tasksService.getTask(id) map {
              case Some(task) =>
                tasksService.softDeleteTask(task) map (_ => ())
              case None => ()
            }) { _ =>
              complete(Ok)
            }
          case _ => complete(Ok)
        }
      } ~ put {
        entity(as[TaskUpdate]) { taskUpdate =>
          lazy val taskNotFound = complete(404, Fail(Error.TaskDoesntExist))
          Try(taskId.toInt) match {
            case Success(id) =>
              onSuccessUnwrap(tasksService.validateUpdateTask(id, taskUpdate) flatMap {
                case Some(task) =>
                  tasksService.updateTask(task) map { _ => complete(Ok) }
                case None => Future {
                  taskNotFound
                }
              })
            case _ => taskNotFound
          }
        }
      }
    } ~
    path("tasks" / Segment / "placeBefore" / Segment) { (taskId, otherTaskId) =>
      auxPlaceMethod(taskId, otherTaskId, (task, otherTask) => {
        tasksService.placeTaskBefore(task, otherTask)
      })
    } ~
    path("tasks" / Segment / "placeAfter" / Segment) { (taskId, otherTaskId) =>
      auxPlaceMethod(taskId, otherTaskId, (task, otherTask) => {
        tasksService.placeTaskAfter(task, otherTask)
      })
    }

  private def auxPlaceMethod(taskId: String, otherTaskId: String, f: (Task, Task) => Future[Any]) = {
    onSuccessUnwrap(
      getTasks(taskId, otherTaskId) flatMap {
        optTasks => {
          if (optTasks.exists(_.isEmpty)) {
            Future {
              complete(404, Fail(Error.TaskDoesntExist))
            }
          }
          else {
            val task: Task = optTasks(0).get
            val otherTask = optTasks(1).get
            f(task, otherTask) map {
              _ => complete(Ok)
            }
          }
        }
      }
    )
  }

  private def getTasks(taskId: String, otherTaskId: String): Future[Seq[Option[Tables.Task]]] = {
    Future.sequence(
      Seq(taskId, otherTaskId)
        .map(strId => {
          Try(strId.toInt) match {
            case Success(id) =>
              tasksService.getTask(id)
            case _ => Future {
              None
            }
          }
        })
    )
  }
}
