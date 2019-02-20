package it_tests.utils

import java.io.File

import scala.sys.process._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

class AppController(ns: String, resourceName: String) {
  import scala.concurrent.ExecutionContext.Implicits.global

  def launchTestDeployment(): Unit = {
    val file = new File(resourceName)
    val path = file.getAbsolutePath

    val res = Future(s"kubectl apply -f $path".!!)
    res onComplete {
      case Failure(ex) =>
        println(s"kubectl apply failed with error: $ex")
      case Success(x) =>
        println(s"kubectl apply applied successfully: $x")
    }

    Await.result(res, Duration.Inf)
  }

  def cleanUpTestDeployment(): Unit = {
    val file = new File(resourceName)
    val path = file.getAbsolutePath

    val res =  Future(s"kubectl delete -f $path".!!)
    res onComplete {
      case Failure(ex) =>
        println(s"kubectl delete failed with error: $ex")
      case Success(x) =>
        println(s"kubectl delete applied successfully: $x")
    }

    Await.result(res, Duration.Inf)
  }

//  private def convertMultiYamlToJson(resourceName: String): List[JSONObject] = {
//    val input = new FileInputStream(new File(resourceName))
//    val yaml: Yaml = new Yaml
//    val yamlIterator = yaml.loadAll(input)
//
//    Stream.continually()
//      .takeWhile(_ => yamlIterator.iterator.hasNext)
//      .map(_ => yamlIterator.iterator.next)
//      .map(x => new JSONObject(x.asInstanceOf[util.LinkedHashMap[String, Object]]))
//      .toList
//  }
}
