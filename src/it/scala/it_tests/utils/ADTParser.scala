/*

package it_tests.utils

import cats.syntax.functor._
import io.circe.Decoder
import io.circe.generic.auto._

import scala.concurrent.duration.Duration

trait K8sResponse
object K8sResponse {
  implicit val decode: Decoder[K8sResponse] = Decoder[CustomObject].widen or Decoder[K8sStatus].widen
}

object ADTParserExample {
  private val crdStateStream = Observable
    .interval(Duration(500, MILLISECONDS))
    .map(_ => {
      val crdResponse = apiInstance.getNamespacedCustomObject(
        crdGroup,
        crdVersion,
        crdNamespace,
        crdPlural,
        crdInstanceName)

      val JSONResp = new JSONObject(crdResponse.asInstanceOf[LinkedTreeMap[String, Object]])
      val decoded = decode[K8sResponse](JSONResp.toString)

      decoded match {
        case Right(x) => x
        case Left(ex) => ex
      }
    })
    .share


    crdStateStream
      .takeWhile {
        case CustomObject(_, _, _, _, status) => status match {
          case None => false
          case Some(s) => s.applicationState.state != SparkOperatorStatus.RunningState
        }
        case K8sStatus(_, _, _, _, _) => false
      }
      .toBlocking
      .subscribe(x => println(s"Waiting of status ${SparkOperatorStatus.RunningState}: $x"))

    crdStateStream
      .takeWhile {
        case K8sStatus(_, _, _, _, _) => false
        case CustomObject(_, _, _, _, _) => true
      }
      .onErrorResumeNext(_ => Observable.empty)
      .subscribe(x => println(s"Monitoring CRD status while running: $x"))
}

case class CustomObject(apiVersion: String,
                        kind: String,
                        metadata: Metadata,
                        spec: Spec,
                        status: Option[Status]) extends K8sResponse

case class Metadata(creationTimestamp: String,
                    generation: Int,
                    uid: String,
                    resourceVersion: String,
                    name: String,
                    namespace: String,
                    selfLink: String)

case class Spec(mode: String,
                image: String,
                imagePullPolicy: String,
                mainApplicationFile: String,
                mainClass: String,
                driver: Driver,
                executor: Executor,
                sparkVersion: String)

case class Driver(cores: Double,
                  coreLimit: String,
                  memory: String,
                  serviceAccount: String,
                  labels: Labels)

case class Executor(cores: Double,
                    instances: Double,
                    memory: String,
                    labels: Labels)

case class Labels(version: String)

case class Status(applicationState: ApplicationState,
                  submissionAttempts: Int,
                  lastSubmissionAttemptTime: String,
                  terminationTime: Option[String],
                  sparkApplicationId: Option[String],
                  executionAttempts: Option[Int],
                  driverInfo: DriverInfo,
                 )

case class DriverInfo(podName: Option[String],
                      webUIPort: Option[Int],
                      webUIServiceName: Option[String])

case class ApplicationState(state: String,
                            errorMessage: String)

case class K8sStatus(apiVersion: String, code: Int, kind: String, reason: String, status: String) extends K8sResponse

*/