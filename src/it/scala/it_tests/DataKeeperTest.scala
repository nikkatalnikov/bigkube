package it_tests

import com.typesafe.config.ConfigFactory
import datakeeper.DataKeeper
import it_tests.utils.SparkController
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.scalatest._
import org.scalatest.concurrent.Eventually

class DataKeeperTest extends FlatSpec with Eventually with Matchers with Inside with TestHelper {
  private val conf = new SparkConf().setAppName("test").setMaster("local[*]")
  implicit val spark: SparkSession = SparkSession
    .builder()
    .config(conf)
    .enableHiveSupport()
    .getOrCreate()

  private val fs = FileSystem.get(new java.net.URI(config.fileSystemUrl), new Configuration())

  private val itConfig = ConfigFactory.load("it.conf")
  private val SparkAppDeployment = itConfig.getString("spark.crd")

  val sparkController = new SparkController(
    "default",
    SparkAppDeployment
  )

  def runApp(): Unit = {
    sparkController.launchSparkTestDeployment()
    sparkController.cleanUpSparkTestDeployment()
  }

  override def beforeAll(): Unit = {
    super.beforeAll()

    executeStatement(s"CREATE SCHEMA IF NOT EXISTS hive.${config.hiveDb}")
    executeStatement(s"DROP TABLE IF EXISTS hive.${config.hiveTable}")

    executeStatement(s"""
      |CREATE TABLE hive.${config.hiveTable} (
      |  field1 integer,
      |  field2 decimal(38,2),
      |  ${DataKeeper.partitionVersionColumn} integer
      |)
      |WITH (
      |  format = 'ORC',
      |  partitioned_by = ARRAY['${DataKeeper.partitionVersionColumn}']
      |)
      """.stripMargin)

    fs.delete(new Path(config.tableDir), true)
  }

  it should "read messages from kafka and store to hdfs" in {
    produceRecords(TestClass(1, 100), TestClass(2, 200), TestClass(3, 200), TestClass(4, 400))

    runApp()

    fs.exists(new Path(s"${config.tableDir}/field2=100")) shouldBe true
    fs.exists(new Path(s"${config.tableDir}/field2=200")) shouldBe true
    fs.exists(new Path(s"${config.tableDir}/field2=400")) shouldBe true

    inside(readTable()) {
      case Seq(r1, r2, r3, r4) =>
        checkRow(r1, "1", "100.00", "1")
        checkRow(r2, "2", "200.00", "1")
        checkRow(r3, "3", "200.00", "1")
        checkRow(r4, "4", "400.00", "1")
    }
  }
}
