package consumer
import consumer.AvroHelper._

import org.apache.spark.sql._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferBrokers
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import com.microsoft.azure.sqldb.spark.config.Config
import com.microsoft.azure.sqldb.spark.connect._

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._

object KafkaConsumer extends LazyLogging {
  private val config = ConfigFactory.load()
  private val localKafkaUrl = config.getString("minikube.kafka.url")
  private val groupId = config.getString("minikube.kafka.groupId")

  private val mssqlConfigMap = Map(
    "url"            -> config.getString("minikube.sqlserver.db.urlForSpark"),
    "driver"         -> config.getString("minikube.sqlserver.db.driver"),
    "databaseName"   -> config.getString("minikube.sqlserver.db.databaseName"),
    "user"           -> config.getString("minikube.sqlserver.db.user"),
    "password"       -> config.getString("minikube.sqlserver.db.password"),
    "connectTimeout" -> config.getString("minikube.sqlserver.db.connectionTimeout")
  )

  private val topics = config
    .getStringList("minikube.kafka.topics")
    .asScala
    .toSet

  private val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> localKafkaUrl,
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[ByteArrayDeserializer],
    "auto.offset.reset" -> "earliest",
    "group.id" -> groupId,
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  val sparkConf: SparkConf = new SparkConf().setAppName("KafkaAvroToHDFSWriter")
  val spark: SparkSession = SparkSession
    .builder()
    .config(sparkConf)
    .enableHiveSupport()
    .getOrCreate()

  val ssc = new StreamingContext(spark.sparkContext, Seconds(5))

  def main(args: Array[String]) {
    val stream = KafkaUtils.createDirectStream[String, Array[Byte]](
      ssc,
      PreferBrokers,
      Subscribe[String, Array[Byte]](topics, kafkaParams)
    )

    import spark.implicits._

    val msgSinkConf = Config(mssqlConfigMap.updated("dbTable", "tbl_msg"))
    val userSinkConf = Config(mssqlConfigMap.updated("dbTable", "tbl_user"))

    stream
      .foreachRDD(rdd => {
        val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

        if (!rdd.isEmpty) {
          val normalizedData = rdd
            .map(x => deserializeMsg(x.value))
            .map(_.normalize())

          normalizedData
            .map(_._1)
            .toDF
            .write
            .mode(SaveMode.Append)
            .sqlDB(msgSinkConf)

          normalizedData
            .map(_._2)
            .toDF
            .write
            .mode(SaveMode.Append)
            .sqlDB(userSinkConf)
        }

        stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
      })

    ssc.start()
    ssc.awaitTermination()
  }

}

