package nom.bruno.tasksservice.services

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Guice}
import nom.bruno.tasksservice.Result
import nom.bruno.tasksservice.Tables.Task
import nom.bruno.tasksservice.routes.{AllRoutes, JsonProtocol}
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, FeatureSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class TasksRoutesTest extends FeatureSpec with JsonProtocol with Matchers with ScalatestRouteTest with BeforeAndAfterEach {
  private[this] val taskService: TasksService = mock(classOf[TasksService])

  private val injector = Guice.createInjector(new AbstractModule {
    override def configure(): Unit = {
      bind(classOf[ExecutionContext]).annotatedWith(Names.named("EC")).toInstance(global)
      bind(classOf[TasksService]).toInstance(taskService)
    }
  })

  val routesService = injector.getInstance(classOf[AllRoutes])

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(taskService)
  }

  feature("get tasks") {
    scenario("succesfull") {
      val expectedTasks = Seq(
        Task(Some(1), "title", "description"),
        Task(Some(2), "title2", "description2"))
      when(taskService.getTasks).thenReturn(Future(expectedTasks))
      Get("/tasks") ~> routesService.routes ~> check {
        status.intValue should equal(200)
        val result = responseAs[Result[Seq[Task]]]
        result.success should be(true)
        result.data.get should be(expectedTasks)
      }
    }
  }
}
