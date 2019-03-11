package consumer

case class User(id: Int, name: String, location: String)
case class MsgNormalized(text: String, description: String, title: String, timestamp: Long, userId: Int)

case class Msg(text: String, description: String, title: String, timestamp: Long, user: User) {
  def normalize(): (MsgNormalized, User) = {
    (MsgNormalized(text, description, title, timestamp, this.user.id), this.user)
  }
}

object Msg {
  def denormalize(pair: (MsgNormalized, User)): Msg = pair match {
    case (m, u) => Msg(m.text, m.description, m.title, m.timestamp, u)
  }
}
