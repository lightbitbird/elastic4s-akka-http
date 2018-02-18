package com.es


import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{Directives, Route}
import com.es.client.GithubClient
import com.es.config.ActorSystemConfig
import com.es.models.JsonSupport
import com.es.services.GitElasticService
import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success}

object ApiRoute extends Directives with ActorSystemConfig with JsonSupport {
  val logger = Logger(getClass.getName)

  def route: Route = pathPrefix("api") {
    path("/") {
      complete(ToResponseMarshallable("200"))
    } ~
      path("data" / Segment) { user =>
        get {
          logger.info("user ==> " + user)

          val restClient = new GithubClient()
          val items = restClient.getResources(user)
          items.onComplete {
            case Success(repoes) => GitElasticService.indexBulk(repoes)
            case Failure(f) => logger.info("Error: " + f.getMessage)
          }
//          items.foreach(s => println(s"""map -> ${s}"""))

          complete(ToResponseMarshallable(items))

        }
      }
  }

}
