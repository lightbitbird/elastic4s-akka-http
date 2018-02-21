package com.es.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.es.config.ElasticConfig

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

class HttpServer(implicit system: ActorSystem,
                 ec: ExecutionContextExecutor,
                 mate: ActorMaterializer) extends ElasticConfig {

  val host = Try(config.getString("service.host")).getOrElse("127.0.0.1")
  val port = Try(config.getInt("service.port")).getOrElse(5000)

  def run() = {
    Http().bindAndHandle(route, host, port)
  }

  def route: Route = {
    GithubApiRoute.gitRoute
  }

}
