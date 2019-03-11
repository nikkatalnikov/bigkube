package consumer

import com.typesafe.config.{Config, ConfigFactory}
import slick.basic.DatabaseConfig
import slick.jdbc.SQLServerProfile
import slick.jdbc.SQLServerProfile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.Await

case class MSSQLService(path: String, forConf: Config = ConfigFactory.load()) {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val dbConfig: DatabaseConfig[SQLServerProfile] = DatabaseConfig.forConfig(path, forConf)
  private val db: SQLServerProfile#Backend#Database = dbConfig.db

  private class Users(tag: Tag) extends Table[User](tag, "tbl_user") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def location = column[String]("location")

    def * = (id, name, location) <> (User.tupled, User.unapply)
  }

  private class Msgs(tag: Tag) extends Table[MsgNormalized](tag, "tbl_msg") {
    def text = column[String]("text")
    def description = column[String]("description")
    def title = column[String]("title")
    def timestamp = column[Long]("timestamp")
    def userId = column[Int]("userId")

    def * = (text, description, title, timestamp, userId) <> (MsgNormalized.tupled, MsgNormalized.unapply)
  }

  private val users = TableQuery[Users]
  private val msgs = TableQuery[Msgs]

  def getRandomUser: User = {
    val q = users.take(1)
    val f = db.run(q.result)

    Await.result(f, Duration.Inf).head
  }

  def getUserMsgs(userId: Int): Seq[Msg] = {
    val pair = for {
      (m, u) <- msgs join users on (_.userId === _.id)
    } yield (m, u)

    val f = db.run(pair.result)

    Await.result(f.map(x => x.map(Msg.denormalize)), Duration.Inf)
  }
}

