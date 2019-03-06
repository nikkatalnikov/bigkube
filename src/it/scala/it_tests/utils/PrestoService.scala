package it_tests.utils

import java.sql.{Connection, DriverManager, ResultSet}
import java.util.Properties

import com.typesafe.config.ConfigFactory

object PrestoService {
  private val config = ConfigFactory.load("it")

  private val properties = new Properties()
  properties.setProperty("user", config.getString("prestodb.db.user"))
  properties.setProperty("password", config.getString("prestodb.db.password"))

  private val url = config.getString("prestodb.db.url")

  private val connection: Connection = DriverManager.getConnection(url, properties)

  def execStatement[T](sql: String, f: ResultSet => T): Stream[T] = {
    val statement = connection.createStatement()
    val rs = statement.executeQuery(sql)

    val result = Stream
      .continually()
      .takeWhile(_ => rs.next)
      .map(_ => f(rs))

    statement.close()
    result
  }
}