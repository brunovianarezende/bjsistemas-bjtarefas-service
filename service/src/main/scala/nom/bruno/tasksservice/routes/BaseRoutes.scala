package nom.bruno.tasksservice.routes

import javax.inject.Inject

import akka.http.scaladsl.server._

trait BaseRoutes extends JsonProtocol {
  def routes: Route
}

class AllRoutes @Inject()(tasksRoutes: TasksRoutes)
  extends BaseRoutes {
  def routes = {
    tasksRoutes.routes
  }
}
