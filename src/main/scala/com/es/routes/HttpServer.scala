package com.es.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.{Directives, ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import com.es.config.ElasticConfig
import com.es.models.{ErrorResponse, JsonSupport}
import com.typesafe.scalalogging.Logger
import org.elasticsearch.transport.RemoteTransportException
import spray.json._

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

class HttpServer(implicit system: ActorSystem,
                 ec: ExecutionContextExecutor,
                 mate: ActorMaterializer) extends Directives with JsonSupport with ElasticConfig {
  val logger = Logger(getClass.getName)

  val host = Try(config.getString("service.host")).getOrElse("127.0.0.1")
  val port = Try(config.getInt("service.port")).getOrElse(5000)

  implicit def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case remote: RemoteTransportException =>
        val errorResponse = ErrorResponse(BadRequest.intValue, "IndexNotFoundException", "Bad request: no such index")
        complete(BadRequest, HttpEntity(ContentTypes.`application/json`, errorResponse.toJson.toString))

      case e: Throwable =>
        extractUri { uri =>
          logger.error(s"""${e}""")
          val errorResponse = ErrorResponse(InternalServerError.intValue, "Internal Server Error", "Internal Server Error occured!!")
          complete(InternalServerError, HttpEntity(ContentTypes.`application/json`, errorResponse.toJson.toString))
        }
    }

  def run() = {
    Http().bindAndHandle(route, host, port)
  }

  def route: Route = {
    GithubApiRoute.route
  }

}
