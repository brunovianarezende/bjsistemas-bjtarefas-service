package nom.bruno.tasksservice.routes

import javax.inject.Inject

import akka.http.scaladsl.server._
import nom.bruno.tasksservice.{Error, Result}

trait BaseRoutes extends JsonProtocol {
  def routes: Route

  def Ok[T](content: T): Result[T] = Result[T](true, Some(content), None)
  def Ok: Result[Unit] = Result(true, None, None)
  def Fail(error: Error): Result[Unit] = Result(false, None, Some(List(error)))
}

class AllRoutes @Inject()(tasksRoutes: TasksRoutes)
  extends BaseRoutes {
  def routes: Route = tasksRoutes.routes
}
