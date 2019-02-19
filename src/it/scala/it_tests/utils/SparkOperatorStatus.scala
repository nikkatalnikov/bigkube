package it_tests.utils

object SparkOperatorStatus extends Enumeration {
  val NewState              = ""
  val SubmittedState        = "SUBMITTED"
  val RunningState          = "RUNNING"
  val CompletedState        = "COMPLETED"
  val FailedState           = "FAILED"
  val FailedSubmissionState = "SUBMISSION_FAILED"
  val PendingRerunState     = "PENDING_RERUN"
  val InvalidatingState     = "INVALIDATING"
  val SucceedingState       = "SUCCEEDING"
  val FailingState          = "FAILING"
  val UnknownState          = "UNKNOWN"
}
