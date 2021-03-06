package nom.bruno.tasksservice.repositories

import nom.bruno.tasksservice.Tables
import nom.bruno.tasksservice.Tables.Task
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, FunSuite, Matchers}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class TaskRepositoryTest extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  protected implicit def executor: ExecutionContext = Implicits.global

  lazy val db: Database = Database.forConfig("mysql")
  var task1 = Task(None, "First task", "This is the first task")
  var task2 = Task(None, "Second task", "This is the second task")

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    //    Await.result(db.run(DBIOAction.seq(Tables.tasks.schema.create)), Duration.Inf)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    db.close()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    val addTasks = (Tables.tasks returning Tables.tasks.map(_.id)) ++= Seq(task1, task2)
    val tasksIds = Await.result(db.run(addTasks), Duration.Inf)
    this.task1 = this.task1.copy(id = Some(tasksIds.head))
    this.task2 = this.task2.copy(id = Some(tasksIds(1)))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    val cleanUpActions = DBIO.seq(Tables.tasks.delete)
    Await.result(db.run(cleanUpActions), Duration.Inf)
  }

  test("get all tasks") {
    val taskRepository = new TaskRepository(db)
    val tasks = Await.result(taskRepository.getAllTasks, Duration.Inf)
    tasks should be(Seq(task1, task2))
  }

  test("delete task") {
    val taskRepository = new TaskRepository(db)
    val numDeleted = Await.result(taskRepository.deleteTask(task1.id.get), Duration.Inf)
    numDeleted should be(1)
    val tasks = Await.result(taskRepository.getAllTasks, Duration.Inf)
    tasks should be(Seq(task2))
  }

  test("trying to delete a task that doesn't exist shouldn't cause a problem") {
    val taskRepository = new TaskRepository(db)
    val numDeleted = Await.result(taskRepository.deleteTask(-1), Duration.Inf)
    numDeleted should be(0)
    val tasks = Await.result(taskRepository.getAllTasks, Duration.Inf)
    tasks should be(Seq(task1, task2))
  }

  test("mark as deleted") {
    val taskRepository = new TaskRepository(db)
    val numChanged = Await.result(taskRepository.markAsDeleted(task1.id.get), Duration.Inf)
    numChanged should be(1)
    val tasks = Await.result(taskRepository.getAllTasks, Duration.Inf)
    tasks should be(Seq(task1.copy(deleted = true), task2))
  }

  test("get task") {
    val taskRepository = new TaskRepository(db)
    val task = Await.result(taskRepository.getTask(task1.id.get), Duration.Inf)
    task should be(Some(task1))
  }

  test("get task that doesn't exist") {
    val taskRepository = new TaskRepository(db)
    val task = Await.result(taskRepository.getTask(-1), Duration.Inf)
    task should be(None)
  }

  test("update task") {
    val taskRepository = new TaskRepository(db)
    val updatedTask = task1.copy(title = "New Title", description = "New description")
    Await.result(taskRepository.updateTask(updatedTask), Duration.Inf)
    val task = Await.result(taskRepository.getTask(task1.id.get), Duration.Inf)
    task should be(Some(updatedTask))
  }


  test("trying to update a task that doesn't exist shouldn't cause a problem") {
    val taskRepository = new TaskRepository(db)
    val task = Task(Some(-1), "a", "b")
    val numUpdated = Await.result(taskRepository.updateTask(task), Duration.Inf)
    numUpdated should be(0)
    val tasks = Await.result(taskRepository.getAllTasks, Duration.Inf)
    tasks should be(Seq(task1, task2))
  }

  test("Add task") {
    val taskRepository = new TaskRepository(db)
    val task = Task(None, "a", "b")
    val newId = Await.result(taskRepository.addTask(task), Duration.Inf)
    assert(newId > 0)
    val newTask = task.copy(id = Some(newId))
    val tasks = Await.result(taskRepository.getAllTasks, Duration.Inf)
    tasks.sortBy(_.id) should be(Seq(task1, task2, newTask))
  }
}

class TaskRepositorySearchTest extends FunSuite with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  protected implicit def executor: ExecutionContext = Implicits.global

  lazy val db: Database = Database.forConfig("mysql")
  var task1 = Task(None, "First task", "This is the first task")
  var task2 = Task(None, "Second task", "This is the second task")
  var task3 = Task(None, "Deleted task", "This is a deleted task", deleted = true)

  override protected def afterAll(): Unit = {
    super.afterAll()
    db.close()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    val addTasks = (Tables.tasks returning Tables.tasks.map(_.id)) ++= Seq(task1, task2, task3)
    val tasksIds = Await.result(db.run(addTasks), Duration.Inf)
    this.task1 = this.task1.copy(id = Some(tasksIds.head))
    this.task2 = this.task2.copy(id = Some(tasksIds(1)))
    this.task3 = this.task3.copy(id = Some(tasksIds(2)))
  }

  override protected def afterEach(): Unit = {
    super.afterEach()
    val cleanUpActions = DBIO.seq(Tables.tasks.delete)
    Await.result(db.run(cleanUpActions), Duration.Inf)
  }

  test("get non deleted tasks") {
    val taskRepository = new TaskRepository(db)
    val tasks = Await.result(taskRepository.getTasks(deleted = false), Duration.Inf)
    tasks should be(Seq(task1, task2))
  }

  test("get deleted tasks") {
    val taskRepository = new TaskRepository(db)
    val tasks = Await.result(taskRepository.getTasks(deleted = true), Duration.Inf)
    tasks should be(Seq(task3))
  }

}