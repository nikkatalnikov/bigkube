package consumer

import java.io.ByteArrayOutputStream
import com.sksamuel.avro4s._
import com.typesafe.scalalogging.LazyLogging

object AvroHelper extends LazyLogging {
  private val schema = AvroSchema[Msg]

  def deserializeMsg(raw: Array[Byte]): Msg = {
    val stream = AvroInputStream.data[Msg].from(raw).build(schema)
    val result = stream.iterator.toStream.head

    stream.close()
    result
  }

  def serializeMsg(seq: Msg): Array[Byte] = {
    val baos = new ByteArrayOutputStream()
    val stream = AvroOutputStream.data[Msg].to(baos).build(schema)

    stream.write(seq)
    stream.flush()
    stream.close()

    val result = baos.toByteArray
    baos.close()
    result
  }

}
