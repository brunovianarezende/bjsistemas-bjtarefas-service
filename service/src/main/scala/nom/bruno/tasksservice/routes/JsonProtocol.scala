package nom.bruno.tasksservice.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import nom.bruno.tasksservice.{Error, Result}
import nom.bruno.tasksservice.Tables.Task
import spray.json.{DefaultJsonProtocol, JsonFormat}

trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val errorFormat = jsonFormat1(Error.apply)

  implicit def resultFomat[T: JsonFormat] = jsonFormat3(Result.apply[T])

  implicit val taskFormat = jsonFormat3(Task.apply)
}
