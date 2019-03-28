package it_tests.utils

import com.fasterxml.jackson.annotation.{JsonCreator, JsonProperty}
import io.fabric8.kubernetes.client.CustomResource
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

// take a look at https://github.com/GoogleCloudPlatform/spark-on-k8s-operator/blob/master/pkg/apis/sparkoperator.k8s.io/v1beta1/types.go
// validate Strings with /it_tests/utils/SparkOperatorEnums.scala and require() - see below
// remove all @JsonIgnoreProperties and add some if not exists
// NOTE: if there's "omitempty" pragma in go json parser, use Option - check all "omitempty"
// add all definitions from types.go

case class CustomObject @JsonCreator() (@JsonProperty("spec") spec: Spec,
                                        @JsonProperty("status") status: Option[Status]) extends CustomResource

@JsonIgnoreProperties(Array("monitoring"))
case class Spec @JsonCreator() (@JsonProperty("type") `type`: String,
                                @JsonProperty("mode") mode: String,
                                @JsonProperty("image") image: String,
                                @JsonProperty("imagePullPolicy") imagePullPolicy: String,
                                @JsonProperty("mainApplicationFile") mainApplicationFile: String,
                                @JsonProperty("mainClass") mainClass: String,
                                @JsonProperty("restartPolicy") restartPolicy: RestartPolicy,
                                @JsonProperty("driver") driver: Driver,
                                @JsonProperty("executor") executor: Executor,
                                @JsonProperty("hadoopConf") hadoopConf: Option[Map[String, String]],
                                @JsonProperty("sparkConf") sparkConf: Option[Map[String, String]],
                                @JsonProperty("sparkVersion") sparkVersion: String) {
  // and so on
  require(ClusterMode.contains(mode))
}

@JsonIgnoreProperties(Array("envSecretKeyRefs"))
case class Driver @JsonCreator() (@JsonProperty("cores") cores: Double,
                                  @JsonProperty("coreLimit") coreLimit: String,
                                  @JsonProperty("memory") memory: String,
                                  @JsonProperty("serviceAccount") serviceAccount: String,
                                  @JsonProperty("labels") labels: Labels)

case class Executor @JsonCreator() (@JsonProperty("cores") cores: Double,
                                    @JsonProperty("instances") instances: Double,
                                    @JsonProperty("memory") memory: String,
                                    @JsonProperty("labels") labels: Labels)

case class Labels @JsonCreator() (@JsonProperty("version") version: String)
case class RestartPolicy @JsonCreator() (@JsonProperty("type") `type`: String)

case class Status @JsonCreator() (@JsonProperty("applicationState") applicationState: ApplicationState,
                                  @JsonProperty("submissionAttempts") submissionAttempts: Int,
                                  @JsonProperty("lastSubmissionAttemptTime") lastSubmissionAttemptTime: String,
                                  @JsonProperty("terminationTime") terminationTime: Option[String],
                                  @JsonProperty("sparkApplicationId") sparkApplicationId: Option[String],
                                  @JsonProperty("executionAttempts") executionAttempts: Option[Int],
                                  @JsonProperty("driverInfo") driverInfo: DriverInfo)

case class DriverInfo @JsonCreator() (@JsonProperty("podName") podName: Option[String],
                                      @JsonProperty("webUIPort") webUIPort: Option[Int],
                                      @JsonProperty("webUIServiceName") webUIServiceName: Option[String])

case class ApplicationState @JsonCreator() (@JsonProperty("state") state: String,
                                            @JsonProperty("errorMessage") errorMessage: String) {}
