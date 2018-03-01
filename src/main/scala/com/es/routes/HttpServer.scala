package com.es.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import com.es.config.ElasticConfig
import StatusCodes._
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

class HttpServer(implicit system: ActorSystem,
                 ec: ExecutionContextExecutor,
                 mate: ActorMaterializer) extends Directives with ElasticConfig {
  val logger = Logger(getClass.getName)

  val host = Try(config.getString("service.host")).getOrElse("127.0.0.1")
  val port = Try(config.getInt("service.port")).getOrElse(5000)

  implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e: Throwable =>
        extractUri { uri =>
          logger.error(s"""${e}""")
          logger.error(s"Request to $uri could not be handled normally")
          complete(HttpResponse(InternalServerError, entity = "Bad request Exception occured!!!"))
        }
    }

  def run() = {
    Http().bindAndHandle(route, host, port)
  }

  def route: Route = {
    GithubApiRoute.route
  }

}
