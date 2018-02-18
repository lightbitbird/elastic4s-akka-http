package com.es


import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{Directives, Route}
import com.es.client.GithubClient
import com.es.config.ElasticConfig
import com.es.models.{GitRepo, JsonSupport}
import com.es.services.ElasticService
import com.typesafe.scalalogging.Logger

import scala.util.{Failure, Success}

trait GithubApiRoute extends Directives with ElasticConfig with JsonSupport {
  service: ElasticService[GitRepo, Long] =>

  val logger = Logger(getClass.getName)

  def route: Route = pathPrefix("api") {
    path("data" / Segment) { user =>
      get {
        logger.info("user ==> " + user)

        val restClient = new GithubClient()
        val items = restClient.getResources(user)
        items.onComplete {
          case Success(repoes) => indexBulk(repoes)
          case Failure(f) => logger.info("Error: " + f.getMessage)
        }
        //items.foreach(s => println(s"""map -> ${s}"""))

        complete(ToResponseMarshallable(items))

      }
    }
  }

}
