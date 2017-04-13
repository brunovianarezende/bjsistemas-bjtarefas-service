package nom.bruno.tasksservice.routes

import javax.inject.Inject

import akka.http.scaladsl.server._
import nom.bruno.tasksservice.Result

trait BaseRoutes extends JsonProtocol {
  def routes: Route

  def Ok[T](content: T) = Result[T](true, Some(content), None)

}

class AllRoutes @Inject()(tasksRoutes: TasksRoutes)
  extends BaseRoutes {
  def routes = {
    tasksRoutes.routes
  }
}
