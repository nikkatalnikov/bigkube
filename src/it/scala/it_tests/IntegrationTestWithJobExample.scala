package it_tests

import com.typesafe.config.ConfigFactory
import org.scalatest._
import com.microsoft.sqlserver.jdbc.SQLServerException

import it_tests.utils._

class IntegrationTestWithJobExample extends FunSuite with BeforeAndAfterAll with Matchers {

  private val config = ConfigFactory.load("it.conf")
  private val JDBCSqlConnStr = config.getString("sqlserver.db.url")
  private val JDBCDriver = config.getString("sqlserver.driver")
  private val DBTestDeployment = config.getString("deployment.it.mssql")
  private val SparkAppDeployment = config.getString("deployment.it.sparkOperator")
  private val SparkTestURL = config.getString("deployment.it.localSparkConf")

  val appController = new AppController("mssql-it", "default", DBTestDeployment)
  val sparkController = new SparkController(
    "MSSQL-poller-it",
    "default",
    this.getClass.getName,
    SparkAppDeployment,
    SparkTestURL
  )

  override def beforeAll(): Unit = {
    try {
      appController.launchTestDeployment()
    } catch {
      case _ => appController.cleanUpTestDeployment()
    }

    try {
      sparkController.launchSparkTestDeployment()
    } catch {
      case _ => sparkController.cleanUpSparkTestDeployment()
    }
  }

//  test("Queries existing tables from seeded MSSQL instance") {
//    val countriesDF = sparkController.spark.read
//      .format("JDBC")
//      .options(
//        Map(
//          "driver" -> JDBCDriver,
//          "url" -> JDBCSqlConnStr,
//          "dbtable" -> "fake.dbo.countries"
//        )
//      )
//      .load()
//
//    val transactionsDF  = sparkController.spark.read
//      .format("JDBC")
//      .options(
//        Map(
//          "driver" -> JDBCDriver,
//          "url" -> JDBCSqlConnStr,
//          "dbtable" -> "fake.dbo.transactions"
//        )
//      )
//      .load()
//
//    countriesDF.count() + transactionsDF.count() should equal (5 + 1000)
//  }

  test("Throws exception if wrong table name provided") {
    val nonExistingTableDF  = sparkController.spark.read
      .format("JDBC")
      .options(
        Map(
          "driver" -> JDBCDriver,
          "url" -> JDBCSqlConnStr,
          "dbtable" -> "fake.dbo.nosuchtable"
        )
      )

    an[SQLServerException] should be thrownBy nonExistingTableDF.load()

  }

  override def afterAll(): Unit = {
    sparkController.shutDownSpark()
    sparkController.cleanUpSparkTestDeployment()
    appController.cleanUpTestDeployment()
  }
}
