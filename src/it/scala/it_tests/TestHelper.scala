package it_tests

import com.typesafe.config.ConfigFactory
import datakeeper.{DataKeeper, DataKeeperConfig}
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import it_tests.utils.PrestoService
import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.spark.sql.avro.SchemaConverters
import org.apache.spark.sql.types.{DecimalType, IntegerType, StructType}
import org.scalatest.{Assertion, BeforeAndAfterAll, Matchers, Suite}

import collection.JavaConverters._
import scala.language.postfixOps

trait TestHelper extends BeforeAndAfterAll with Matchers {
  this: Suite =>

  private val defaultConfig = ConfigFactory.parseResources("datakeeper.conf")
  private val testConfig = ConfigFactory.parseResources("it.conf")
    .withFallback(defaultConfig)
    .resolve()

  val config = DataKeeperConfig(testConfig)
  val partitionCount = 3

  private val client = AdminClient.create(config.kafkaParams.asJava)
  private val topicConf = new NewTopic(config.topic, partitionCount, 1)

  private val schema = new StructType()
    .add("field1", IntegerType)
    .add("field2", DecimalType(38, 2))

  private val schemaRegistryClient =
    new CachedSchemaRegistryClient(config.kafkaParams("schema.registry.url").asInstanceOf[String], 128)

  private val avroSchema = SchemaConverters.toAvroType(schema)

  override def beforeAll(): Unit = {
    client.createTopics(Set(topicConf).asJava)
    schemaRegistryClient.register(config.topic + "-test", avroSchema)
    println(s"topic ${config.topic} created")
  }

  override def afterAll(): Unit = {
    client.deleteTopics(Set(config.topic).asJava)
    schemaRegistryClient.deleteSubject(config.topic + "-test")
    println(s"topic ${config.topic} dropped")
  }

  def checkRow(columns: Map[String, String], f1: String, f2: String, v: String): Assertion =
    columns should contain theSameElementsAs Seq(
      "field1" -> f1,
      "field2" -> f2,
      DataKeeper.partitionVersionColumn -> v)

  def executeStatement(sql: String): Boolean =
    PrestoService.execStatement(sql)

  def executeQuery(sql: String): Stream[Map[String, String]] =
    PrestoService.execQuery(sql, x => Map(
      "field1" -> x.getString(1),
      "field2" -> x.getString(2),
      DataKeeper.partitionVersionColumn -> x.getString(3)))

  def readTable(): Stream[Map[String, String]] =
    executeQuery(s"SELECT * FROM ${config.hiveTable} as t ORDER BY field1")

  def readTable(whereClause: String): Stream[Map[String, String]] =
    executeQuery(s"SELECT * FROM ${config.hiveTable} as t WHERE $whereClause ORDER BY field1")

  def produceRecords(records: TestClass*): Unit = {
    records
      .foreach(record => record.toAvroDF
        .write
        .format("kafka")
        .option("kafka.bootstrap.servers", config.kafkaParams("bootstrap.servers").asInstanceOf[String])
        .option("topic", config.topic)
        .save()
      )
  }
}
