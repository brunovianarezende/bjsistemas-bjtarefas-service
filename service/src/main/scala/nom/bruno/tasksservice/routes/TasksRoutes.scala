package nom.bruno.tasksservice.routes

import javax.inject.Inject

import akka.http.scaladsl.server.Route
import nom.bruno.tasksservice.services.TasksService

class TasksRoutes @Inject()(tasksService: TasksService) extends BaseRoutes {
  override def routes: Route = ???
}
