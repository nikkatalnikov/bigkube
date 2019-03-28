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
  require(DeployMode.contains(mode))
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


case class RestartPolicy @JsonCreator() (@JsonProperty("type") `type`: Option[String],
                                         @JsonProperty("onSubmissionFailureRetries") onSubmissionFailureRetries: Option[Int],
                                         @JsonProperty("onFailureRetries") onFailureRetries: Option[Int],
                                         @JsonProperty("onSubmissionFailureRetryInterval") onSubmissionFailureRetryInterval: Option[Int],
                                         @JsonProperty("onFailureRetryInterval") onFailureRetryInterval: Option[Int]){
  require(RestartPolicyType.contains(`type`.get))
}


case class Status @JsonCreator() (@JsonProperty("applicationState") applicationState: ApplicationState,
                                  @JsonProperty("submissionAttempts") submissionAttempts: Int,
                                  @JsonProperty("lastSubmissionAttemptTime") lastSubmissionAttemptTime: String,
                                  @JsonProperty("terminationTime") terminationTime: Option[String],
                                  @JsonProperty("sparkApplicationId") sparkApplicationId: Option[String],
                                  @JsonProperty("executionAttempts") executionAttempts: Option[Int],
                                  @JsonProperty("driverInfo") driverInfo: DriverInfo)

case class ScheduledSparkApplication @JsonCreator() (@JsonProperty("scheduledSparkApplicationSpec") scheduledSparkApplicationSpec: Spec,
                                                     @JsonProperty("scheduledSparkApplicationStatus") scheduledSparkApplicationStatus: Option[Status])

case class ScheduledSparkApplicationSpec @JsonCreator() (@JsonProperty("schedule") schedule: String,
                                                         @JsonProperty("template") template: SparkApplicationSpec,
                                                         @JsonProperty("suspend") suspend: Boolean,
                                                         @JsonProperty("concurrencyPolicy") concurrencyPolicy: Option[String],
                                                         @JsonProperty("successfulRunHistoryLimit") successfulRunHistoryLimit: Option[Int],
                                                         @JsonProperty("failedRunHistoryLimit") failedRunHistoryLimit: Option[Int]) {
  require(ConcurrencyPolicy.contains(concurrencyPolicy.get))
}

case class ScheduledSparkApplicationStatus @JsonCreator() (@JsonProperty("lastRunName") lastRunName: String,
                                                           @JsonProperty("pastSuccessfulRunNames") pastSuccessfulRunNames: Option[Array[String]],
                                                           @JsonProperty("pastFailedRunNames") pastFailedRunNames: Option[Array[String]],
                                                           @JsonProperty("scheduleState") scheduleState: Option[String],
                                                           @JsonProperty("reason") reason: Option[String]) {
  require(ScheduleState.contains(scheduleState.get))
}

case class ScheduledSparkApplicationList @JsonCreator() (@JsonProperty("items") items: Option[Array[ScheduledSparkApplication]])


case class SparkApplication @JsonCreator() (@JsonProperty("sparkApplicationSpec") sparkApplicationSpec: Spec,
                                            @JsonProperty("scheduledSparkApplicationStatus") scheduledSparkApplicationStatus: Option[SparkApplicationSpec])

case class SparkApplicationSpec @JsonCreator() (@JsonProperty("type") `type`: String,
                                                @JsonProperty("sparkVersion") sparkVersion: String,
                                                @JsonProperty("mode") mode: Option[String],
                                                @JsonProperty("image") image: Option[String],
                                                @JsonProperty("initContainerImage") initContainerImage: Option[String],
                                                @JsonProperty("imagePullPolicy") imagePullPolicy: Option[String],
                                                @JsonProperty("imagePullSecrets") imagePullSecrets: Option[Array[String]],
                                                @JsonProperty("mainClass") mainClass: Option[String],
                                                @JsonProperty("mainApplicationFile") mainApplicationFile: String,
                                                @JsonProperty("arguments") arguments: Option[Array[String]],
                                                @JsonProperty("sparkConf") sparkConf: Option[Map[String,String]],
                                                @JsonProperty("hadoopConf") hadoopConf: Option[Map[String,String]],
                                                @JsonProperty("sparkConfigMap") sparkConfigMap: Option[String],
                                                @JsonProperty("hadoopConfigMap") hadoopConfigMap: Option[String],
//                                                @JsonProperty("volumes") volumes: Option[Volume],
                                                @JsonProperty("driver") driver: DriverSpec,
                                                @JsonProperty("executor") executor: ExecutorSpec,
                                                @JsonProperty("deps") deps: Dependencies,
                                                @JsonProperty("restartPolicy") restartPolicy: Option[RestartPolicy],
                                                @JsonProperty("nodeSelector") nodeSelector: Option[Map[String,String]],
                                                @JsonProperty("failureRetries") failureRetries: Option[Int],
                                                @JsonProperty("retryInterval") retryInterval: Option[Int],
                                                @JsonProperty("pythonVersion") pythonVersion: Option[String],
                                                @JsonProperty("memoryOverheadFactor") memoryOverheadFactor: Option[String],
                                                @JsonProperty("monitoring") monitoring: Option[MonitoringSpec]) {
  require(SparkApplicationType.contains(`type`))
  require(DeployMode.contains(mode.get))
}

case class ApplicationState @JsonCreator() (@JsonProperty("state") state: String,
                                            @JsonProperty("errorMessage") errorMessage: Option[String]) {
  require(ApplicationStateType.contains(state))
}

case class SparkApplicationStatus @JsonCreator() (@JsonProperty("sparkApplicationId") sparkApplicationId: Option[String],
                                                  @JsonProperty("submissionID") submissionID: Option[String],
                                                  @JsonProperty("driverInfo") driverInfo: DriverInfo,
                                                  @JsonProperty("appState") app: Option[ApplicationState],
                                                  @JsonProperty("executorState") executorState: Option[Map[String,String]],
                                                  @JsonProperty("executionAttempts") executionAttempts: Option[Int],
                                                  @JsonProperty("submissionAttempts") submissionAttempts: Option[Int]){

  executorState
    .get
    .valuesIterator
    .foreach(value =>
      require(ExecutorState.contains(value))
    )
}

case class SparkApplicationList @JsonCreator() (@JsonProperty("items") items: Option[Array[SparkApplication]])

case class Dependencies @JsonCreator() (@JsonProperty("jars") jars: Option[Array[String]],
                                        @JsonProperty("files") files: Option[Array[String]],
                                        @JsonProperty("pyFiles") pyFiles: Option[Array[String]],
                                        @JsonProperty("jarsDownloadDir") jarsDownloadDir: Option[String],
                                        @JsonProperty("filesDownloadDir") filesDownloadDir: Option[String],
                                        @JsonProperty("downloadTimeout") downloadTimeout: Option[Int],
                                        @JsonProperty("maxSimultaneousDownloads") maxSimultaneousDownloads: Option[Int])

case class SparkPodSpec @JsonCreator() (@JsonProperty("cores") cores: Option[Float],
                                        @JsonProperty("coreLimit") coreLimit: Option[String],
                                        @JsonProperty("memory") memory: Option[String],
                                        @JsonProperty("memoryOverhead") memoryOverhead: Option[String],
                                        @JsonProperty("image") image: Option[String],
                                        @JsonProperty("configMaps") configMaps: Option[Array[NamePath]],
                                        @JsonProperty("secrets") secrets: Option[Array[SecretInfo]],
                                        @JsonProperty("envVars") envVars: Option[Map[String,String]],
                                        @JsonProperty("envSecretKeyRefs") envSecretKeyRefs: Option[Map[String,String]],
                                        @JsonProperty("labels") labels: Option[Map[String,String]],
                                        @JsonProperty("annotations") annotations: Option[Map[String,String]])

case class DriverSpec @JsonCreator() (@JsonProperty("podName") podName: Option[String],
//                                        @JsonProperty("sparkPodSpec") sparkPodSpec: Option[ApplicationState],
                                        @JsonProperty("serviceAccount") serviceAccount: Option[String],
                                        @JsonProperty("javaOptions") javaOptions: Option[String])

case class ExecutorSpec @JsonCreator() (@JsonProperty("instances") instances: Option[Int],
//                                        @JsonProperty("sparkPodSpec") sparkPodSpec: Option[ApplicationState],
                                        @JsonProperty("coreRequest") coreRequest: Option[String],
                                        @JsonProperty("javaOptions") javaOptions: Option[String])

case class NamePath @JsonCreator() (@JsonProperty("name") name: String,
                                    @JsonProperty("path") path: String)

case class DriverInfo @JsonCreator() (@JsonProperty("webUIServiceName") webUIServiceName: Option[String],
                                      @JsonProperty("webUIPort") webUIPort: Option[Int],
                                      @JsonProperty("webUIAddress") webUIAddress: Option[String],
                                      @JsonProperty("webUIIngressName") webUIIngressName: Option[String],
                                      @JsonProperty("webUIIngressAddress") webUIIngressAddress: Option[String],
                                      @JsonProperty("podName") podName: Option[String])

case class SecretInfo @JsonCreator() (@JsonProperty("name") name: String,
                                      @JsonProperty("path") path: String,
                                      @JsonProperty("type") `type`: String){
  require(SecretType.contains(`type`))
}

case class NameKey @JsonCreator() (@JsonProperty("name") name: String,
                                   @JsonProperty("key") key: String)


case class MonitoringSpec @JsonCreator() (@JsonProperty("exposeDriverMetrics") exposeDriverMetrics: Boolean,
                                      @JsonProperty("exposeExecutorMetrics") exposeExecutorMetrics: Boolean,
                                      @JsonProperty("metricsProperties") metricsProperties: Option[String],
                                      @JsonProperty("prometheus") prometheus: Option[PrometheusSpec])

case class PrometheusSpec @JsonCreator() (@JsonProperty("jmxExporterJar") jmxExporterJar: String,
                                          @JsonProperty("port") port: Int,
                                          @JsonProperty("configFile") configFile: Option[String],
                                          @JsonProperty("configuration") configuration: Option[String])