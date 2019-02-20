package it_tests

import com.typesafe.config.ConfigFactory
import org.scalatest._
import com.microsoft.sqlserver.jdbc.SQLServerException
import it_tests.utils._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

class IntegrationTestWithJobExample extends FunSuite with BeforeAndAfterAll with Matchers {
  private val sparkConf = new SparkConf().setAppName("mssql-it").setMaster("local")
  lazy val spark: SparkSession = SparkSession
    .builder()
    .config(sparkConf)
    .getOrCreate()

  private val config = ConfigFactory.load("it.conf")
  private val JDBCSqlConnStr = config.getString("sqlserver.db.url")
  private val JDBCDriver = config.getString("sqlserver.driver")
  private val DBTestDeployment = config.getString("deployment.it.mssql")
  private val SparkAppDeployment = config.getString("deployment.it.sparkOperator")
  private val k8sUrl = config.getString("deployment.it.k8sUrl")

  val appController = new AppController( "default", DBTestDeployment)
  val sparkController = new SparkController(
    "default",
    SparkAppDeployment,
    k8sUrl
  )

  override def beforeAll(): Unit = {
    try {
      appController.launchTestDeployment()
    } catch {
      case _: Throwable => appController.cleanUpTestDeployment()
    }

    try {
      sparkController.launchSparkTestDeployment()
    } catch {
      case _: Throwable => sparkController.cleanUpSparkTestDeployment()
    }
  }

//  test("Queries existing tables from seeded MSSQL instance") {
//    val countriesDF = spark.read
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
//    val transactionsDF  = spark.read
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
    val nonExistingTableDF  = spark.read
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
    spark.stop()
    sparkController.cleanUpSparkTestDeployment()
    appController.cleanUpTestDeployment()
  }
}
