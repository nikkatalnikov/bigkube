package producer

object SparkRunner {
  def main(args: Array[String]): Unit = {
    MSSQLConnection.query()
    MSSQLConnection.spark.stop()
  }
}
