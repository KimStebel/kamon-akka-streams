package com.ovoenergy.kamon.akka.streams

import akka.stream._
import akka.stream.scaladsl._
import akka.NotUsed
import akka.actor.ActorSystem

import scala.concurrent._
import scala.concurrent.duration._

import akka.testkit.TestKit
import kamon.metric.instrument.CollectionContext
import org.scalatest._

class ThroughputSpec extends TestKit(ActorSystem("MySpec")) with FlatSpecLike with Matchers {

  implicit val materializer = ActorMaterializer()
  val ints: Source[Int, NotUsed] = Source(1 to 100)

  "The Throughput metrics collector" should "collect throughput" in {
    val collector = ThroughputCollector("myStream")

    val streamFuture = ints.via(collector.flow).runWith(Sink.ignore)
    Await.ready(streamFuture, Duration.Inf)

    val cc = CollectionContext(0)
    collector.counter.collect(cc).count should be(100)

  }
}
