package nom.bruno.tasksservice.routes

import javax.inject.Inject

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import nom.bruno.tasksservice.services.TasksService

class TasksRoutes @Inject()(tasksService: TasksService) extends BaseRoutes {
  override def routes: Route = path("tasks") {
    get {
      onSuccess(tasksService.getTasks) {tasks =>
        complete(Ok(tasks))
      }
    }
  }
}
