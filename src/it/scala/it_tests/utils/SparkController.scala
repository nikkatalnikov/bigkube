package it_tests.utils

import org.json.JSONObject
import org.yaml.snakeyaml.Yaml
import java.io.{File, FileInputStream}
import java.util

import com.google.gson.internal.LinkedTreeMap
import io.kubernetes.client.apis.CustomObjectsApi
import io.kubernetes.client.util.Config
import io.kubernetes.client.Configuration
import io.kubernetes.client.models._
import rx.lang.scala.Observable
import io.circe.generic.auto._
import io.circe.parser._

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import scala.concurrent.duration._

class SparkController(crdNamespace: String, resourceName: String) {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val crdGroup = "sparkoperator.k8s.io"
  private val crdVersion = "v1beta1"
  private val crdPlural = "sparkapplications"
  private val crdInstanceName = "spark-pi"

  private val apiClient = Config.defaultClient
//  apiClient.setDebugging(true)
  Configuration.setDefaultApiClient(apiClient)
  private val apiInstance  = new CustomObjectsApi
  private val customObjectBody = convertYamlToJson(resourceName)

  private val crdStateStream = Observable
    .interval(Duration(1000, MILLISECONDS))
    .map(_ => {
      val crdResponse = apiInstance.getNamespacedCustomObject(
        crdGroup,
        crdVersion,
        crdNamespace,
        crdPlural,
        crdInstanceName)

      val JSONResp = new JSONObject(crdResponse.asInstanceOf[LinkedTreeMap[String, Object]])
      val decoded = decode[CustomObject](JSONResp.toString)

      decoded match {
        case Right(x) => x
        case Left(ex) => throw new RuntimeException(ex)
      }
    })
    .distinctUntilChanged(x => x.status)
    .share

  def launchSparkTestDeployment(): Unit = {
    val apiCall =
      Future(apiInstance
        .createNamespacedCustomObject(
          crdGroup,
          crdVersion,
          crdNamespace,
          crdPlural,
          customObjectBody.toMap,
          "false"))

    apiCall onComplete {
      case Failure(ex) =>
        throw new RuntimeException(ex)
      case Success(x) =>
        println(s"applied successfully: $x")
    }

    Await.result(apiCall, Duration.Inf)

    crdStateStream
      .takeWhile(x => {
        x.status match {
          case None => true
          case Some(status) => status.applicationState.state != SparkOperatorStatus.RunningState
        }
      })
      .toBlocking
      .subscribe(x => println(s"Waiting for status ${SparkOperatorStatus.RunningState}: $x"))

    crdStateStream
      .takeWhile(x => {
        x.status match {
          case None => false
          case Some(status) => status.applicationState.state != SparkOperatorStatus.CompletedState
        }
      })
      .onErrorResumeNext(_ => Observable.empty)
      .subscribe(x => println(s"Monitoring CRD status while running: $x"))
  }

  def cleanUpSparkTestDeployment(): Unit = {
    val apiCall =
      Future(apiInstance
        .deleteNamespacedCustomObject(
          crdGroup,
          crdVersion,
          crdNamespace,
          crdPlural,
          crdInstanceName,
          new V1DeleteOptions,
          0,
          false,
          "Foreground"))

    apiCall onComplete {
      case Failure(ex) =>
        println(s"failed with error: $ex")
      case Success(x) =>
        println(s"applied successfully: $x")
    }

    Await.result(apiCall, Duration.Inf)
  }

  private def convertYamlToJson(resourceName: String): JSONObject = {
    val input = new FileInputStream(new File(resourceName))
    val yaml: Yaml = new Yaml
    val source: util.LinkedHashMap[String, Object] = yaml.load(input)

    new JSONObject(source)
  }
}
