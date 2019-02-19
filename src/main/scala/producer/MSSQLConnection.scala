package producer

import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.SparkConf
import org.apache.spark.sql._

object MSSQLConnection extends LazyLogging {

  private val config = ConfigFactory.load()
  private val jdbcSqlConnStr = config.getString("sqlserver.db.url")

  private val sparkConf = new SparkConf().setAppName("MSSQL-poller")
  val spark: SparkSession = SparkSession
    .builder()
    .config(sparkConf)
    .getOrCreate()

  def query(): Unit = {

    val countriesDF = spark.read
      .format("jdbc")
      .options(
        Map(
          "driver" -> config.getString("sqlserver.driver"),
          "url" -> jdbcSqlConnStr,
          "dbtable" -> "fake.dbo.countries"
        )
      )
      .load()

    val transactionsDF  = spark.read
      .format("jdbc")
      .options(
        Map(
          "driver" -> config.getString("sqlserver.driver"),
          "url" -> jdbcSqlConnStr,
          "dbtable" -> "fake.dbo.transactions"
        )
      )
      .load()

    countriesDF.show()
    transactionsDF.show()
  }
}
