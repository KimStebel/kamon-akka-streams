package com.ovoenergy.kamon.akka.streams

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.testkit.TestKit
import kamon.metric.instrument.CollectionContext
import org.scalatest._

import scala.concurrent._
import scala.concurrent.duration._

class LatencySpec extends TestKit(ActorSystem("MySpec")) with FlatSpecLike with Matchers {

  implicit val materializer = ActorMaterializer()
  implicit val ec: ExecutionContext = implicitly[ActorSystem].dispatcher
  val ints: Source[Int, NotUsed] = Source(1 to 10)

  "The Latency metrics collector" should "collect metrics" in {
    val collector = LatencyCollector("myStream")

    val flow: Flow[Int, Int, NotUsed] = Flow.fromFunction(x => {
      Thread.sleep(100)
      x
    })

    implicit val id = new Identifiable[Int, Int] {
      override type C = Int
      override def fromA(a: Int) = a
      override def fromB(b: Int) = b
    }

    val streamFuture = ints.via(collector.flow(flow)).runWith(Sink.ignore)
    Await.ready(streamFuture, Duration.Inf)

    val cc = CollectionContext(100)
    collector.histogram.collect(cc).min shouldBe >= (100L)

  }
}
