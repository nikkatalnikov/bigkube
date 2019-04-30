package datakeeper

import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.spark.streaming.kafka010.OffsetRange
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class OffsetManager(kafkaParams: Map[String, AnyRef], topic: String, maxMessagesPerPartition: Int) {
  @transient private[this] val logger = LoggerFactory.getLogger(this.getClass)

  private val consumer = new KafkaConsumer[String, GenericRecord](kafkaParams.asJava)

  def getOffsetRanges(initialOffsets: Array[(TopicPartition, Long)] = Array.empty): Array[OffsetRange] = {
    val partitions = Option(consumer.partitionsFor(topic))
      .map { listPartitions =>
        listPartitions.asScala.map(p => new TopicPartition(p.topic, p.partition))
      }
      .getOrElse {
        throw new RuntimeException(s"No such Topic $topic")
      }

    consumer.assign(partitions.asJava)

    val startOffsets =
      if (initialOffsets.isEmpty) partitions.map(p => p -> consumer.position(p))
      else initialOffsets.toList

    consumer.seekToEnd(partitions.asJava)

    val endOffsets = partitions.map(p => p -> consumer.position(p)).toMap
    val maxRanges = startOffsets.map { case (p, startOffset) => OffsetRange(p, startOffset, endOffsets(p)) }.toArray
    maxRanges.map { range =>
      if (range.untilOffset - range.fromOffset > maxMessagesPerPartition) {
        logger.warn(
          s"Too many records in kafka ${range.topicPartition()}: ${range.untilOffset - range.fromOffset}. " +
            s"Read just $maxMessagesPerPartition records.")

        consumer.seek(range.topicPartition(), range.fromOffset + maxMessagesPerPartition)
        OffsetRange(range.topicPartition(), range.fromOffset, range.fromOffset + maxMessagesPerPartition)
      } else {
        range
      }
    }
  }

  def commitOffsets(): Map[TopicPartition, Long] = {
    val committedOffsets = consumer.assignment.asScala.map(p => p -> consumer.position(p)).toMap
    consumer.commitSync()
    consumer.close()
    committedOffsets
  }
}
