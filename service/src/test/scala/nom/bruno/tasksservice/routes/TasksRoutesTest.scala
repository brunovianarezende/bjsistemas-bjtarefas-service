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
  // with this "pretty" hack now IDEA will stop marking as invalid the valid tests...
  implicit private def hackToFixIDEA = TildeArrow.injectIntoRoute

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

  feature("move a task before other") {
    scenario("move a task before other") {
      val task1: Task = Task(Some(1), "title", "description")
      val task2: Task = Task(Some(2), "title2", "description2")
      when(taskService.getTask(task1.id.get)).thenReturn(Future(Some(task1)))
      when(taskService.getTask(task2.id.get)).thenReturn(Future(Some(task2)))
      when(taskService.placeTaskBefore(task1, task2)).thenReturn(Future(()))
      Post(s"/tasks/${task1.id.get}/placeBefore/${task2.id.get}") ~>
        routesService.routes ~> check {
        status.intValue should equal(200)
        val result = responseAs[Result[Unit]]
        result.success should be(true)
        verify(taskService, times(1)).placeTaskBefore(task1, task2)
      }
    }

    scenario("move task that doesn't exist") {
      val id = 1
      when(taskService.getTask(id)).thenReturn(Future(None))
      val task2: Task = Task(Some(2), "title2", "description2")
      when(taskService.getTask(task2.id.get)).thenReturn(Future(Some(task2)))
      Post(s"/tasks/$id/placeBefore/${task2.id.get}") ~>
        routesService.routes ~> check {
        status.intValue should equal(404)
        val result = responseAs[Result[Unit]]
        result.success should be(false)
        result.errors should be(Some(List(Error.TaskDoesntExist)))
        verify(taskService, never()).placeTaskBefore(any(), any())
      }
    }

    scenario("move task before a task that doesn't exist") {
      val task1: Task = Task(Some(1), "title", "description")
      val id = 2
      when(taskService.getTask(id)).thenReturn(Future(None))
      when(taskService.getTask(task1.id.get)).thenReturn(Future(Some(task1)))
      Post(s"/tasks/${task1.id.get}/placeBefore/$id") ~>
        routesService.routes ~> check {
        status.intValue should equal(404)
        val result = responseAs[Result[Unit]]
        result.success should be(false)
        result.errors should be(Some(List(Error.TaskDoesntExist)))
        verify(taskService, never()).placeTaskBefore(any(), any())
      }
    }

    scenario("move task with invalid id") {
      val id = "invalid"
      val task2: Task = Task(Some(2), "title2", "description2")
      when(taskService.getTask(task2.id.get)).thenReturn(Future(Some(task2)))
      Post(s"/tasks/$id/placeBefore/${task2.id.get}") ~>
        routesService.routes ~> check {
        status.intValue should equal(404)
        val result = responseAs[Result[Unit]]
        result.success should be(false)
        result.errors should be(Some(List(Error.TaskDoesntExist)))
        verify(taskService, never()).placeTaskBefore(any(), any())
      }
    }

    scenario("move task before a task with invalid id") {
      val task1: Task = Task(Some(1), "title", "description")
      when(taskService.getTask(task1.id.get)).thenReturn(Future(Some(task1)))
      Post(s"/tasks/${task1.id.get}/placeBefore/invalid") ~>
        routesService.routes ~> check {
        status.intValue should equal(404)
        val result = responseAs[Result[Unit]]
        result.success should be(false)
        result.errors should be(Some(List(Error.TaskDoesntExist)))
        verify(taskService, never()).placeTaskBefore(any(), any())
      }
    }
  }

  feature("move a task after other") {
    scenario("move a task after other") {
      val task1: Task = Task(Some(1), "title", "description")
      val task2: Task = Task(Some(2), "title2", "description2")
      when(taskService.getTask(task1.id.get)).thenReturn(Future(Some(task1)))
      when(taskService.getTask(task2.id.get)).thenReturn(Future(Some(task2)))
      when(taskService.placeTaskAfter(task1, task2)).thenReturn(Future(()))
      Post(s"/tasks/${task1.id.get}/placeAfter/${task2.id.get}") ~>
        routesService.routes ~> check {
        status.intValue should equal(200)
        val result = responseAs[Result[Unit]]
        result.success should be(true)
        verify(taskService, times(1)).placeTaskAfter(task1, task2)
      }
    }

    scenario("move task that doesn't exist") {
      val id = 1
      when(taskService.getTask(id)).thenReturn(Future(None))
      val task2: Task = Task(Some(2), "title2", "description2")
      when(taskService.getTask(task2.id.get)).thenReturn(Future(Some(task2)))
      Post(s"/tasks/$id/placeAfter/${task2.id.get}") ~>
        routesService.routes ~> check {
        status.intValue should equal(404)
        val result = responseAs[Result[Unit]]
        result.success should be(false)
        result.errors should be(Some(List(Error.TaskDoesntExist)))
        verify(taskService, never()).placeTaskAfter(any(), any())
      }
    }

    scenario("move task after a task that doesn't exist") {
      val task1: Task = Task(Some(1), "title", "description")
      val id = 2
      when(taskService.getTask(id)).thenReturn(Future(None))
      when(taskService.getTask(task1.id.get)).thenReturn(Future(Some(task1)))
      Post(s"/tasks/${task1.id.get}/placeAfter/$id") ~>
        routesService.routes ~> check {
        status.intValue should equal(404)
        val result = responseAs[Result[Unit]]
        result.success should be(false)
        result.errors should be(Some(List(Error.TaskDoesntExist)))
        verify(taskService, never()).placeTaskAfter(any(), any())
      }
    }

    scenario("move task with invalid id") {
      val id = "invalid"
      val task2: Task = Task(Some(2), "title2", "description2")
      when(taskService.getTask(task2.id.get)).thenReturn(Future(Some(task2)))
      Post(s"/tasks/$id/placeAfter/${task2.id.get}") ~>
        routesService.routes ~> check {
        status.intValue should equal(404)
        val result = responseAs[Result[Unit]]
        result.success should be(false)
        result.errors should be(Some(List(Error.TaskDoesntExist)))
        verify(taskService, never()).placeTaskAfter(any(), any())
      }
    }

    scenario("move task after a task with invalid id") {
      val task1: Task = Task(Some(1), "title", "description")
      when(taskService.getTask(task1.id.get)).thenReturn(Future(Some(task1)))
      Post(s"/tasks/${task1.id.get}/placeAfter/invalid") ~>
        routesService.routes ~> check {
        status.intValue should equal(404)
        val result = responseAs[Result[Unit]]
        result.success should be(false)
        result.errors should be(Some(List(Error.TaskDoesntExist)))
        verify(taskService, never()).placeTaskAfter(any(), any())
      }
    }
  }
}
