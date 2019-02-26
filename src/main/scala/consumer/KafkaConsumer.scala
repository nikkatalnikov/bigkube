package consumer
import consumer.AvroHelper._

import org.apache.spark.sql._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import com.typesafe.scalalogging.LazyLogging
import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

object KafkaConsumer extends LazyLogging {
  private val config = ConfigFactory.load()
  private val localKafkaUrl = config.getString("minikube.kafka.url")
  private val topics = config
    .getStringList("minikube.kafka.topics")
    .asScala
    .toSet

  private val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> localKafkaUrl,
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer]
  )

  def main(args: Array[String]) {
    val sparkConf = new SparkConf().setAppName("KafkaAvroToHDFSWriter")
    val spark: SparkSession = SparkSession
      .builder()
      .config(sparkConf)
      .getOrCreate()

    val ssc = new StreamingContext(sparkConf, Seconds(2))

    val stream = KafkaUtils.createDirectStream[String,  Array[Byte]](
      ssc,
      PreferConsistent,
      Subscribe[String, Array[Byte]](topics, kafkaParams)
    )

    stream
      .map(record => (record.key, record.value))
      .foreachRDD((rdd, time) => {
        val parsed = rdd.map { case (_, v) => deserializeMsg(v) }

        logger.info(s"PROCESSED RDD at $time: $parsed")
        logger.info(s"Deserialized tweets: ${parsed.count}")

        import spark.implicits._

        parsed
          .toDF
          .write
          .mode(SaveMode.Append)
          .format("parquet")
          .saveAsTable("test")
      })

    ssc.start()
    ssc.awaitTermination()
  }

}

