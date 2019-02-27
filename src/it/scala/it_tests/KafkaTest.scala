package it_tests

import it_tests.utils._
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.{ByteArraySerializer, StringSerializer}
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.util.{Failure, Success}
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class KafkaTest extends FunSuite with BeforeAndAfterAll with Matchers {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val config = ConfigFactory.load("it")
  private val localKafkaUrl = config.getString("kafka.url")

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
  }

  test("it reads data from Kafka topics and stores it to hdfs") {
    val recordsNumber = 100
    val records = FakeMsgGenerator.generateNFakeMsgs(recordsNumber)
    val fs = topics
      .map(t => (t, consumer.AvroHelper.serializeMsg(records)))
      .map {
        case (t, array) => new ProducerRecord[String, Array[Byte]](t, array)
      }
      .map(x => Future(producer.send(x)))

    val seqF = Future.sequence(fs)

    seqF onComplete {
      case Success(v) => v
      case Failure(e) => throw new RuntimeException(s"Producer error $e")
    }

    Await.result(seqF, Duration.Inf)
    producer.close()

    import PrestoService.dbConfig.profile.api._
    lazy val verifyQ = sqlu"""select count(*) from test"""
    val qF = PrestoService.db.run(verifyQ)

    qF onComplete {
      case Success(v) => assert(v == recordsNumber)
      case Failure(e) => throw new RuntimeException(s"Presto query error $e")
    }

    Await.result(qF, Duration.Inf)
  }

  override def afterAll(): Unit = {
    sparkController.cleanUpSparkTestDeployment()
  }
}

