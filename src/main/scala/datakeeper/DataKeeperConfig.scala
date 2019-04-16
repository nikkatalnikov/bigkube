package datakeeper

import com.typesafe.config.Config
import org.apache.kafka.common.TopicPartition
import org.apache.spark.SparkConf

import scala.collection.JavaConverters._
import scala.util.Try

case class DataKeeperConfig(
  fileSystemUrl: String,
  tableDir: String,
  hiveTable: String,
  numOfOutputFiles: Int,
  format: String,
  idColumns: Seq[String],
  partitioningColumns: Seq[String],
  sortColumns: Seq[String],
  sparkConf: SparkConf,
  topic: String,
  kafkaParams: Map[String, Object],
  zooKeeperSettings: ZooKeeperSettings,
  initialOffset: Map[TopicPartition, Long] = Map.empty,
  maxMessagesPerPartition: Int,
  partitionsParallelism: Option[Int])
{
  val hiveDb :: hiveTablename :: Nil = hiveTable.split('.').toList
}

case class ZooKeeperSettings(url: String, sessionTimeoutMs: Int, connectionTimeoutMs: Int)

object DataKeeperConfig {

  def apply(config: Config): DataKeeperConfig = {

    val sparkConf: SparkConf = new SparkConf()
      .setAppName(config.getString("spark.app.name"))
      .set("spark.serializer", config.getString("spark.serializer"))

    val hiveTable = config.getString("hive.tableName")

    val topic = config.getString("kafka.topic")
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> config.getString("kafka.url"),
      "key.deserializer"          -> config.getString("kafka.key.deserializer"),
      "value.deserializer"        -> config.getString("kafka.value.deserializer"),
      "key.serializer"            -> config.getString("kafka.key.serializer"),
      "value.serializer"          -> config.getString("kafka.value.serializer"),
      "auto.offset.reset"         -> config.getString("kafka.offsetMode"),
      "group.id"                  -> config.getString("kafka.groupId"),
      "schema.registry.url"       -> config.getString("kafka.schema.registry.url"),
      "enable.auto.commit"        -> Boolean.box(config.getBoolean("kafka.autocommit"))
    )

    val fileSystemUrl: String = config.getString("fs.url")
    val dir: String = config.getString("fs.dir")
    val format: String = config.getString("fs.format")
    val numOfOutputFiles: Int = config.getInt("fs.numOfOutputFiles")

    val idColumns: Seq[String] = config.getStringList("columns.identity").asScala
    val partitioningColumns: Seq[String] = config.getStringList("columns.partitioning").asScala
    val sortColumns: Seq[String] = config.getStringList("columns.sorting").asScala

    val initialOffsetsPropertiesName = "kafka.initialOffsets"
    val initialOffset: Map[TopicPartition, Long] = if (config.hasPath(initialOffsetsPropertiesName)) {
      config
        .getConfigList(initialOffsetsPropertiesName)
        .asScala
        .map(c => (new TopicPartition(topic, c.getInt("partition")), c.getLong("initialOffset")))
        .toMap
    } else Map.empty

    val maxMessagesPerPartition = config.getInt("execution.maxMessagesPerPartition")
    val partitionsParallelism = Try(config.getInt("execution.partitionsParallelism")).toOption

    val zooKeeperSettings = ZooKeeperSettings(
      url = config.getString("zookeeper.url"),
      sessionTimeoutMs = config.getInt("zookeeper.session-timeout.ms"),
      connectionTimeoutMs = config.getInt("zookeeper.connection-timeout.ms")
    )

    new DataKeeperConfig(
      fileSystemUrl = fileSystemUrl,
      format = format,
      tableDir = dir,
      numOfOutputFiles = numOfOutputFiles,
      hiveTable = hiveTable,
      partitioningColumns = partitioningColumns,
      idColumns = idColumns,
      sortColumns = sortColumns,
      sparkConf = sparkConf,
      topic = topic,
      kafkaParams = kafkaParams,
      zooKeeperSettings = zooKeeperSettings,
      initialOffset = initialOffset,
      maxMessagesPerPartition = maxMessagesPerPartition,
      partitionsParallelism = partitionsParallelism
    )
  }
}
