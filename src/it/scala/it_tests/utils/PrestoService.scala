package it_tests.utils

import slick.jdbc.JdbcProfile
import slick.basic.DatabaseConfig

object PrestoService {
  val dbConfig: DatabaseConfig[JdbcProfile] = DatabaseConfig.forConfig("prestodb")
  val db: JdbcProfile#Backend#Database = dbConfig.db
}
