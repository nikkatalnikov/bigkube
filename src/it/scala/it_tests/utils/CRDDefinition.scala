package it_tests.utils

case class CustomObject(apiVersion: String,
                        kind: String,
                        metadata: Metadata,
                        spec: Spec,
                        status: Option[Status])

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
                  driverInfo: DriverInfo
                 )

case class DriverInfo(podName: Option[String],
                      webUIPort: Option[Int],
                      webUIServiceName: Option[String])

case class ApplicationState(state: String,
                            errorMessage: String)
