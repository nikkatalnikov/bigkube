package consumer
import consumer.AvroHelper._

import org.apache.spark.sql._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferBrokers
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config.ConfigFactory

import scala.collection.JavaConverters._

object KafkaConsumer extends LazyLogging {
  private val config = ConfigFactory.load()
  private val localKafkaUrl = config.getString("minikube.kafka.url")
  private val groupId = config.getString("minikube.kafka.groupId")
  private val tableName = config.getString("minikube.hive.tableName")
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

    stream
      .foreachRDD(rdd => {
        val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

        if (!rdd.isEmpty) {
          rdd
            .map(x => deserializeMsg(x.value))
            .toDF
            .write
            .mode(SaveMode.Append)
            .format("parquet")
            .saveAsTable(tableName)
          }

        stream.asInstanceOf[CanCommitOffsets].commitAsync(offsetRanges)
      })

    ssc.start()
    ssc.awaitTermination()
  }

}

