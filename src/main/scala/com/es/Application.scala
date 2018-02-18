package com.es

import akka.http.scaladsl.Http
import com.es.config.{ActorSystemConfig, ElasticConfig}

import scala.util.Try

object Application extends App with ActorSystemConfig with ElasticConfig {
  val host = Try(config.getString("service.host")).getOrElse("127.0.0.1")
  val port = Try(config.getInt("service.port")).getOrElse(9300)

  Http().bindAndHandle(ApiRoute.route, host, port)

}
