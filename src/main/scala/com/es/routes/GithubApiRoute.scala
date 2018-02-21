package com.es.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import com.es.client.GithubClient
import com.es.models.{BaseEntity, GitRepo, JsonSupport}
import com.es.services.GitElasticService
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

trait ApiRoute[T <: BaseEntity[A], A] extends Directives with JsonSupport {

  val logger = Logger(getClass.getName)

  def gitRoute(implicit system: ActorSystem,
               ec: ExecutionContextExecutor,
               materializer: ActorMaterializer): Route

}


object GithubApiRoute extends ApiRoute[GitRepo, Long] {

  override def gitRoute(implicit system: ActorSystem,
                        ec: ExecutionContextExecutor,
                        materializer: ActorMaterializer): Route = pathPrefix("api") {
    val service = new GitElasticService()

    path("data" / Segment) { user =>
      get {
        logger.info("user ==> " + user)

        val restClient = new GithubClient()
        val items = restClient.getResources(user)
        items.onComplete {
          case Success(repoes) => service.indexBulk(repoes)
          case Failure(f) => logger.info("Error: " + f.getMessage)
        }
        //items.foreach(s => println(s"""map -> ${s}"""))
        complete(ToResponseMarshallable(items))
      }
    }
  }

}
