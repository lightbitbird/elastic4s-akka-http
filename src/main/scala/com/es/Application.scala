package com.es

import akka.http.scaladsl.Http
import com.es.services.GitElasticService

import scala.util.Try

object Application extends App with GitElasticService with GithubApiRoute {
  val host = Try(config.getString("service.host")).getOrElse("127.0.0.1")
  val port = Try(config.getInt("service.port")).getOrElse(5000)

  Http().bindAndHandle(route, host, port)

}
