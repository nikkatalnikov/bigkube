package producer

import scala.concurrent.duration._
import rx.lang.scala.Observable

import scala.concurrent._


object SQLPoller {


  def fromObservable[T](observable: Observable[T]): Future[Seq[T]] = {
    val promise: Promise[Seq[T]] = Promise()

    observable
      .toSeq
      .subscribe(x => promise.success(x), e => promise.failure(e), () => println("done"))

    promise.future
  }


  private val obs = Observable.interval(Duration(1000, MILLISECONDS)).take(10)

  val f = fromObservable(obs)
}
