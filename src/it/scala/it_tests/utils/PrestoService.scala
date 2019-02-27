package it_tests.utils

import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcProfile
import slick.basic.DatabaseConfig

object PrestoService {
  private val config = ConfigFactory.load("it")
  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("prestodb", config)
  val db: JdbcProfile#Backend#Database = dbConfig.db
}
