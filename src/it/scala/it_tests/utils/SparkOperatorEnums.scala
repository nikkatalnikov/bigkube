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

object ClusterMode extends EnumOfStrings {
  val ClusterMode          = Value("cluster")
  val ClientMode           = Value("client")
  val InClusterClientMode  = Value("in-cluster-client")
}
