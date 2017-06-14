package nom.bruno.tasksservice.services

import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Guice}
import nom.bruno.tasksservice.{TaskCreation, TaskUpdate}
import nom.bruno.tasksservice.Tables.Task
import nom.bruno.tasksservice.repositories.{TaskRepository, TaskRepositoryDb, TaskRepositoryStub}
import org.mockito.Mockito.{mock, reset, times, verify, when}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

class TasksServiceTest extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  private[this] val taskRepositoryDb: TaskRepository = mock(classOf[TaskRepositoryDb])

  private def injector(taskRepository: TaskRepository = taskRepositoryDb) = Guice.createInjector(new AbstractModule {
    override def configure(): Unit = {
      bind(classOf[ExecutionContext]).annotatedWith(Names.named("EC")).toInstance(global)
      bind(classOf[TaskRepository]).toInstance(taskRepository)
    }
  })


  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(taskRepositoryDb)
  }

  test("get tasks") {
    val expectedTasks = Seq(
      Task(Some(1), "title", "description"),
      Task(Some(2), "title2", "description2"))
    when(taskRepositoryDb.getAllTasks).thenReturn(Future(expectedTasks))
    val service = injector().getInstance(classOf[TasksService])
    Await.result(service.getTasks, Duration.Inf) should be(expectedTasks)
  }

  test("get task") {
    val task = Task(Some(1), "title", "description")
    when(taskRepositoryDb.getTask(task.id.get)).thenReturn(Future(Some(task)))
    val service = injector().getInstance(classOf[TasksService])
    Await.result(service.getTask(task.id.get), Duration.Inf) should be(Some(task))
  }

  test("delete task") {
    val task = Task(Some(1), "title", "description")
    when(taskRepositoryDb.deleteTask(1)).thenReturn(Future {
      1
    })
    val service = injector().getInstance(classOf[TasksService])
    Await.result(service.deleteTask(task), Duration.Inf)
    verify(taskRepositoryDb, times(1)).deleteTask(1)
  }

  test("mark task as deleted") {
    val task = Task(Some(1), "title", "description")
    when(taskRepositoryDb.markAsDeleted(1)).thenReturn(Future {
      1
    })
    val service = injector().getInstance(classOf[TasksService])
    Await.result(service.softDeleteTask(task), Duration.Inf)
    verify(taskRepositoryDb, times(1)).markAsDeleted(1)
  }

  test("update task") {
    val task = Task(Some(1), "title", "description")
    when(taskRepositoryDb.updateTask(task)).thenReturn(Future {
      1
    })
    val service = injector().getInstance(classOf[TasksService])
    Await.result(service.updateTask(task), Duration.Inf)
    verify(taskRepositoryDb, times(1)).updateTask(task)
  }

  test("validate update task") {
    val task = Task(Some(1), "title", "description")
    val taskUpdate = TaskUpdate(Some("new title"), Some("new description"))
    when(taskRepositoryDb.getTask(task.id.get)).thenReturn(Future(Some(task)))
    val service = injector().getInstance(classOf[TasksService])
    val updatedTask = Await.result(service.validateUpdateTask(1, taskUpdate), Duration.Inf)
    updatedTask.get should equal(Task(Some(1), "new title", "new description"))
  }

  test("validate update task - No task found") {
    val task = Task(Some(1), "title", "description")
    val taskUpdate = TaskUpdate(Some("new title"), Some("new description"))
    when(taskRepositoryDb.getTask(task.id.get)).thenReturn(Future(None))
    val service = injector().getInstance(classOf[TasksService])
    val updatedTask = Await.result(service.validateUpdateTask(1, taskUpdate), Duration.Inf)
    updatedTask should be(None)
  }

  test("add new task") {
    val taskData = TaskCreation("title", "description")
    val task = Task(None, "title", "description")
    when(taskRepositoryDb.addTask(task)).thenReturn(Future(1))
    val service = injector().getInstance(classOf[TasksService])
    val newTask = Await.result(service.addTask(taskData), Duration.Inf)
    newTask should equal(task.copy(id = Some(1)))
  }

  test("search for tasks") {
    val expectedTasks = Seq(
      Task(Some(1), "title", "description"),
      Task(Some(2), "title2", "description2"))
    when(taskRepositoryDb.getTasks()).thenReturn(Future(expectedTasks))
    val service = injector().getInstance(classOf[TasksService])
    Await.result(service.searchForTasks, Duration.Inf) should be(expectedTasks)
  }

  test("place task before") {
    val initialTasks = (0 to 4).map(i => Task(Some(i), "title" + i, "description" + i))
    val taskRepository = TaskRepositoryStub(initialTasks)
    val service = injector(taskRepository).getInstance(classOf[TasksService])
    Await.result(service.placeTaskBefore(initialTasks(3), initialTasks(1)), Duration.Inf)
    val expectedIds = Seq(0, 3, 1, 2, 4)
    Await.result(taskRepository.getTasks(), Duration.Inf).map(_.id.get) should be(expectedIds)
  }
}