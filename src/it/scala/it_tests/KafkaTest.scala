package it_tests

import it_tests.utils._
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.util.{Failure, Success}
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class KafkaTest extends FunSuite with BeforeAndAfterAll with Matchers {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val config = ConfigFactory.load()
  private val localKafkaUrl = config.getString("minikube.kafka.url")

  private val topics = config
    .getStringList("minikube.kafka.topics")
    .asScala
    .toSet

  private val SparkAppDeployment = config.getString("spark.crd")

  val sparkController = new SparkController(
    "default",
    SparkAppDeployment,
  )

  private val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> localKafkaUrl,
    "key.serializer" -> classOf[StringSerializer],
    "value.serializer" -> classOf[StringSerializer]
  )

  val producer = new KafkaProducer[String, Array[Byte]](kafkaParams.asJava)

  override def beforeAll(): Unit = {
    val records = FakeMsgGenerator.generateNFakeMsgs(100)

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

    try {
      sparkController.launchSparkTestDeployment()
    } catch {
      case _: Throwable => sparkController.cleanUpSparkTestDeployment()
    }
  }

  override def afterAll(): Unit = {
    sparkController.cleanUpSparkTestDeployment()
  }
}

