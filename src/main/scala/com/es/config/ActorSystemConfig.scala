package com.es.config

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

trait ActorSystemConfig {
  implicit val system = ActorSystem("elastic")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
}
