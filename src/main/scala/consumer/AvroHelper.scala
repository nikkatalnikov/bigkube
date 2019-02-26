package consumer

import java.io.ByteArrayOutputStream
import com.sksamuel.avro4s._
import scala.util.Try

case class User(id: Int, name: String, location: String)
case class Msg(text: String, description: String, title: String, timestamp: Long, user: User)

object AvroHelper {
  private val schema = AvroSchema[User]

  def deserializeMsg(raw: Array[Byte]): List[Try[Msg]] = {
    val stream = AvroInputStream.data[Msg].from(raw).build(schema)
    val result = stream.tryIterator.toList

    stream.close()
    result
  }

  def serializeMsg(seq: Seq[Msg]): Array[Byte] = {
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
