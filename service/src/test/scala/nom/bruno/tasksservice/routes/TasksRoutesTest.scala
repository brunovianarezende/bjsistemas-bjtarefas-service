package nom.bruno.tasksservice.routes

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Guice}
import nom.bruno.tasksservice.Tables.Task
import nom.bruno.tasksservice.services.TasksService
import nom.bruno.tasksservice.{Error, Result, TaskCreation, TaskUpdate, TaskView}
import org.mockito.ArgumentMatchers._
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

  val routesService: AllRoutes = injector.getInstance(classOf[AllRoutes])

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(taskService)
  }

  feature("get tasks") {
    scenario("succesfull") {
      val expectedTasks = Seq(
        Task(Some(1), "title", "description"),
        Task(Some(2), "title2", "description2"))
      when(taskService.searchForTasks).thenReturn(Future(expectedTasks))
      Get("/tasks") ~> routesService.routes ~> check {
        status.intValue should equal(200)
        val result = responseAs[Result[Seq[TaskView]]]
        result.success should be(true)
        val expectedResponse = Seq(
          TaskView(1, "title", "description"),
          TaskView(2, "title2", "description2"))
        result.data.get should be(expectedResponse)
      }
    }
  }

  feature("delete task") {
    scenario("delete task that exists") {
      val task = Task(Some(1), "title", "description")

      when(taskService.getTask(1)).thenReturn(Future(Some(task)))
      when(taskService.softDeleteTask(task)).thenReturn(Future(()))
      Delete("/tasks/1") ~>
        routesService.routes ~> check {
        status.intValue should equal(200)
        val result = responseAs[Result[Unit]]
        result.success should be(true)
        verify(taskService, never()).deleteTask(any())
        verify(taskService, times(1)).softDeleteTask(task)
      }
    }

    scenario("delete task that doesn't exist") {
      when(taskService.getTask(1)).thenReturn(Future(None))
      Delete("/tasks/1") ~>
        routesService.routes ~> check {
        status.intValue should equal(200)
        val result = responseAs[Result[Unit]]
        result.success should be(true)
        verify(taskService, never()).deleteTask(any())
        verify(taskService, never()).softDeleteTask(any())
      }
    }

    scenario("delete task with an invalid id") {
      Delete("/tasks/invalid") ~>
        routesService.routes ~> check {
        status.intValue should equal(200)
        val result = responseAs[Result[Unit]]
        result.success should be(true)
        verify(taskService, never()).deleteTask(any())
        verify(taskService, never()).softDeleteTask(any())
      }
    }
  }

  feature("update task") {
    scenario("update task that exists") {
      val task: Task = Task(Some(1), "title", "description")
      val taskUpdate: TaskUpdate = TaskUpdate(Some("new title"), Some("new description"))
      val updatedTask: Task = task.copy(title = taskUpdate.title.get, description = taskUpdate.description.get)

      when(taskService.validateUpdateTask(1, taskUpdate)).thenReturn(Future(Some(updatedTask)))
      when(taskService.updateTask(updatedTask)).thenReturn(Future(()))

      Put("/tasks/1", taskUpdate) ~>
        routesService.routes ~> check {
        status.intValue should equal(200)
        val result = responseAs[Result[Unit]]
        result.success should be(true)
        verify(taskService, times(1)).updateTask(updatedTask)
      }
    }

    scenario("update task that doesn't exist") {
      val task: Task = Task(Some(1), "title", "description")
      val taskUpdate: TaskUpdate = TaskUpdate(Some("new title"), Some("new description"))

      when(taskService.validateUpdateTask(1, taskUpdate)).thenReturn(Future(None))

      Put("/tasks/1", taskUpdate) ~>
        routesService.routes ~> check {
        status.intValue should equal(404)
        val result = responseAs[Result[Unit]]
        result.success should be(false)
        result.errors should be(Some(List(Error.TaskDoesntExist)))
      }
    }

    scenario("update task with an invalid id") {
      val taskUpdate: TaskUpdate = TaskUpdate(Some("new title"), Some("new description"))

      Put("/tasks/invalid", taskUpdate) ~>
        routesService.routes ~> check {
        status.intValue should equal(404)
        val result = responseAs[Result[Unit]]
        result.success should be(false)
        result.errors should be(Some(List(Error.TaskDoesntExist)))
      }
    }
  }

  feature("add task") {
    scenario("new task") {
      val id = 1
      val title = "title"
      val description = "description"
      val taskData: TaskCreation = TaskCreation(title, description)

      when(taskService.addTask(taskData)).thenReturn(Future(Task(Some(id), title, description)))

      Post("/tasks", taskData) ~>
        routesService.routes ~> check {
        status.intValue should equal(200)
        val result = responseAs[Result[Int]]
        result.success should be(true)
        result.data should be(Some(id))
      }
    }

  }
}
