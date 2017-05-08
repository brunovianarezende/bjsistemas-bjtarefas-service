package nom.bruno.tasksservice.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Guice}
import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.Database

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global


object HttpService extends App {
  private val config = ConfigFactory.load()
  private val injector = Guice.createInjector(new AbstractModule {
    override def configure(): Unit = {
      val db = Database.forConfig("mysql")

      bind(classOf[Database]).toInstance(db)
      bind(classOf[ExecutionContext]).annotatedWith(Names.named("EC")).toInstance(global)
    }
  })

  private val routesService = injector.getInstance(classOf[AllRoutes])

  implicit val system: ActorSystem = ActorSystem()

  implicit val materializer = ActorMaterializer()

  implicit val dispatcher = system.dispatcher

  Http().bindAndHandle(routesService.routes, config.getString("domain"), config.getInt("port"))
}
