package it_tests

import it_tests.utils._
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.collection.JavaConverters._

class KafkaTest extends FunSuite with BeforeAndAfterAll with Matchers {
  private val config = ConfigFactory.load("it")
  private val localKafkaUrl = config.getString("kafka.url")
  private val MSSQLServiceInstance = consumer.MSSQLService("sqlserver", config)
  private val tableName = config.getString("hive.tableName")

  private val topics = config
    .getStringList("kafka.topics")
    .asScala
    .toSet

  private val SparkAppDeployment = config.getString("spark.crd")

  val sparkController = new SparkController(
    "default",
    SparkAppDeployment
  )

  private val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> localKafkaUrl,
    "key.serializer" -> classOf[StringSerializer],
    "value.serializer" -> classOf[ByteArraySerializer]
  )

  val producer = new KafkaProducer[String, Array[Byte]](kafkaParams.asJava)

  override def beforeAll(): Unit = {
    try {
      sparkController.launchSparkTestDeployment()
    } catch {
      case _: Throwable => sparkController.cleanUpSparkTestDeployment()
    }

    val records = FakeMsgGenerator.generateNFakeMsgs(100)
    topics
      .flatMap(t => records
        .map(r => (t, consumer.AvroHelper.serializeMsg(r)))
      )
      .map {
        case (t, array) => new ProducerRecord[String, Array[Byte]](t, array)
      }
      .foreach(x => producer.send(x))

    producer.close()

    // let Spark Streaming fetch latest from Kafka msg and store it
    Thread.sleep(60000)
  }

  test("it reads data from Kafka topics and stores it to hdfs") {
    val singleRowQ = s"SELECT COUNT(*) FROM $tableName"
    val count = PrestoService.execStatement(singleRowQ, _.getInt(1)).head

    count should be > 0
  }

    test("it reads data from Kafka topics and stores it to mssql") {
    val usr = MSSQLServiceInstance.getRandomUser

    println(s"usr: $usr")
    usr shouldBe a [consumer.User]
  }

  test("it reads data from Kafka topics and stores it respecting foreign keys") {
    val usr = MSSQLServiceInstance.getRandomUser
    val msgs = MSSQLServiceInstance.getUserMsgs(usr.id).head

    println(s"msgs joined: $msgs")
    msgs shouldBe a [consumer.Msg]
  }

  override def afterAll(): Unit = {
    sparkController.cleanUpSparkTestDeployment()
  }
}

