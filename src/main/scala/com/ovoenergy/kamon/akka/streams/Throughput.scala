package com.ovoenergy.kamon.akka.streams

import akka.NotUsed
import akka.stream.scaladsl.Flow
import kamon.Kamon
import kamon.metric.instrument.Counter

case class ThroughputCollector(name: String) {
  def flow[A]: Flow[A, A, NotUsed] = Flow.fromFunction(a => {
    counter.increment()
    a
  })
  val counter: Counter = Kamon.metrics.counter(name)
}

