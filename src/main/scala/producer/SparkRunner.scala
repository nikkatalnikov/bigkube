package producer

import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.util._


object SparkRunner  {
  def main(args: Array[String]): Unit = {
    MSSQLConnection.query()
    MSSQLConnection.spark.stop()
  }

//  implicit val ec = ExecutionContext.global
//  val res = SQLPoller.f
//
//  res.onComplete {
//    case Success(value) => println(value.toList)
//    case Failure(e) => e.printStackTrace
//  }
//
//  Await.result(res, Duration.Inf)
//  Thread.sleep(100)
}
