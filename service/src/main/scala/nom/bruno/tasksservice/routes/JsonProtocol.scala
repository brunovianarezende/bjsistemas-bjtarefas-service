package nom.bruno.tasksservice.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
}
