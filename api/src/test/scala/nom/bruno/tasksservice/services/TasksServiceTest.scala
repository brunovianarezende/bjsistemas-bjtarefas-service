package nom.bruno.tasksservice.services

import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Guice}
import nom.bruno.tasksservice.Tables.Task
import nom.bruno.tasksservice.repositories.TaskRepository
import org.mockito.Mockito.{mock, reset, when, verify, times}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class TasksServiceTest extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  private[this] val taskRepository: TaskRepository = mock(classOf[TaskRepository])

  private val injector = Guice.createInjector(new AbstractModule {
    override def configure(): Unit = {
      bind(classOf[ExecutionContext]).annotatedWith(Names.named("EC")).toInstance(global)
      bind(classOf[TaskRepository]).toInstance(taskRepository)
    }
  })


  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(taskRepository)
  }

  test("get tasks") {
    val expectedTasks = Seq(
      Task(Some(1), "title", "description"),
      Task(Some(2), "title2", "description2"))
    when(taskRepository.getAllTasks).thenReturn(Future(expectedTasks))
    val service = injector.getInstance(classOf[TasksService])
    Await.result(service.getTasks, Duration.Inf) should be(expectedTasks)
  }

  test("get task") {
    val task = Task(Some(1), "title", "description")
    when(taskRepository.getTask(task.id.get)).thenReturn(Future(Some(task)))
    val service = injector.getInstance(classOf[TasksService])
    Await.result(service.getTask(task.id.get), Duration.Inf) should be(Some(task))
  }

  test("delete task") {
    val task = Task(Some(1), "title", "description")
    when(taskRepository.deleteTask(1)).thenReturn(Future {
      1
    })
    val service = injector.getInstance(classOf[TasksService])
    Await.result(service.deleteTask(task), Duration.Inf)
    verify(taskRepository, times(1)).deleteTask(1)
  }
}
