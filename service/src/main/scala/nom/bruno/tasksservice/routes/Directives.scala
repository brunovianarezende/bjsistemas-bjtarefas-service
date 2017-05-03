package nom.bruno.tasksservice.routes

import akka.http.scaladsl.server.Directives.onSuccess
import akka.http.scaladsl.server.Route

import scala.concurrent.Future

object Directives {
  def unwrapSuccess(f: Future[Route]): Route = {
    onSuccess(f) { r => r }
  }
}
