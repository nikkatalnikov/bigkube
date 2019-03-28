package it_tests.utils

trait EnumOfStrings extends Enumeration {
  def contains(x: String): Boolean = values.exists(_.toString == x)
}

object SparkOperatorStatus extends EnumOfStrings {
  val NewState              = Value("")
  val SubmittedState        = Value("SUBMITTED")
  val RunningState          = Value("RUNNING")
  val CompletedState        = Value("COMPLETED")
  val FailedState           = Value("FAILED")
  val FailedSubmissionState = Value("SUBMISSION_FAILED")
  val PendingRerunState     = Value("PENDING_RERUN")
  val InvalidatingState     = Value("INVALIDATING")
  val SucceedingState       = Value("SUCCEEDING")
  val FailingState          = Value("FAILING")
  val UnknownState          = Value("UNKNOWN")
}

object DeployMode extends EnumOfStrings {
  val ClusterMode          = Value("cluster")
  val ClientMode           = Value("client")
  val InClusterClientMode  = Value("in-cluster-client")
}

object RestartPolicyType extends EnumOfStrings {
  val Never     = Value("Never")
  val OnFailure = Value("OnFailure")
  val Always    = Value("Always")
}

object SparkApplicationType extends EnumOfStrings {
  val JavaApplicationType   = Value("Java")
  val ScalaApplicationType  = Value("Scala")
  val PythonApplicationType = Value("Python")
  val RApplicationType      = Value("R")
}

object ConcurrencyPolicy extends EnumOfStrings {
  val ConcurrencyAllow   = Value("Allow")
  val ConcurrencyForbid   = Value("Forbid")
  val ConcurrencyReplace   = Value("Replace")
}

object ScheduleState extends EnumOfStrings {
  val FailedValidationState = Value("FailedValidation")
  val ScheduledState        = Value("Scheduled")
}

object ApplicationStateType extends EnumOfStrings {
  val NewState              = Value("")
  val SubmittedState        = Value("SUBMITTED")
  val RunningState          = Value("RUNNING")
  val CompletedState        = Value("COMPLETED")
  val FailedState           = Value("FAILED")
  val FailedSubmissionState = Value("SUBMISSION_FAILED")
  val PendingRerunState     = Value("PENDING_RERUN")
  val InvalidatingState     = Value("INVALIDATING")
  val SucceedingState       = Value("SUCCEEDING")
  val FailingState          = Value("FAILING")
  val UnknownState          = Value("UNKNOWN")
}

object ExecutorState extends EnumOfStrings {
  val ExecutorPendingState   = Value("PENDING")
  val ExecutorRunningState   = Value("RUNNING")
  val ExecutorCompletedState = Value("COMPLETED")
  val ExecutorFailedState    = Value("FAILED")
  val ExecutorUnknownState   = Value("UNKNOWN")
}

object SecretType extends EnumOfStrings {
  val GCPServiceAccountSecret     = Value("GCPServiceAccount")
  val HadoopDelegationTokenSecret = Value("HadoopDelegationToken")
  val GenericType                 = Value("Generic")
}
