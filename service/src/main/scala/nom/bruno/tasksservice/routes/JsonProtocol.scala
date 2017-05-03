package nom.bruno.tasksservice.routes

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import nom.bruno.tasksservice.{ChangeTask, Error, Result}
import nom.bruno.tasksservice.Tables.Task
import spray.json.{DefaultJsonProtocol, JsonFormat}

trait JsonProtocol extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val errorFormat = jsonFormat2(Error.apply)

  implicit def resultFomat[T: JsonFormat] = jsonFormat3(Result.apply[T])

  implicit val taskFormat = jsonFormat3(Task.apply)

  implicit val changeTaskFormat = jsonFormat2(ChangeTask.apply)
}
