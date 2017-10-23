package com.ovoenergy.kamon.akka.streams

import java.time.Instant

import akka.NotUsed
import akka.actor.{Actor, ActorSystem, Props}
import akka.stream.scaladsl.Flow
import akka.util.Timeout
import akka.pattern.ask
import com.ovoenergy.kamon.akka.streams.TimingActor.{Start, Stop}
import kamon.Kamon
import kamon.metric.instrument.Histogram

import scala.concurrent.duration._
import scala.collection.mutable
import scala.concurrent.ExecutionContext

trait Identifiable[-A, -B] {
  type C
  def fromA(a: A): C
  def fromB(b: B): C
}

object TimingActor {
  case class Start[K](key: K)
  case class Stop[K](key: K)
}

class TimingActor[K] extends Actor {
  import TimingActor._

  val map = mutable.Map[K, Long]()
  def cleanUp() {} //TODO

  def receive = {
    case Start(key) => {
      map.put(key.asInstanceOf[K], System.currentTimeMillis())
    }
    case Stop(key) => {
      val startTime = map.remove(key.asInstanceOf[K])
      sender ! startTime
    }
  }
}

case class LatencyCollector(name: String)(implicit ec: ExecutionContext) {

  implicit val timeout = Timeout(5.seconds)

  def flow[A,B](flow: Flow[A, B, NotUsed])(implicit identifiable: Identifiable[A, B], actorSystem: ActorSystem): Flow[A, B, NotUsed] = {
    lazy val actorRef = actorSystem.actorOf(Props[TimingActor[identifiable.C]])
    Flow.fromFunction((a: A) => {
      actorRef ! Start(identifiable.fromA(a))
      a
    }).via(flow).mapAsync(1) { b =>
      val end = System.currentTimeMillis()
      val r = actorRef ? Stop(identifiable.fromB(b))
      r.map(start => {
        val startO = start.asInstanceOf[Option[Long]]
        startO.foreach(start => {
          val latency = end - start
          histogram.record(latency)
        })
        b
      })
    }
  }

  val histogram: Histogram = Kamon.metrics.histogram(name)

  def shutdown = ??? //TODO
}

