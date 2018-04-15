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

  def route(implicit system: ActorSystem,
            ec: ExecutionContextExecutor,
            materializer: ActorMaterializer): Route
}


object GithubApiRoute extends ApiRoute[GitRepo, Long] {

  override def route(implicit system: ActorSystem,
                     ec: ExecutionContextExecutor,
                     materializer: ActorMaterializer): Route = pathPrefix("api") {

    path("data" / Segment) { user =>
      get {
        logger.info("user ==> " + user)

        val restClient = new GithubClient()
        val items = restClient.getResources(user)
        onComplete(items) {
          case Success(repoes) =>
            val bulk = GitElasticService.indexBulk(repoes)
            onComplete(bulk) {
              case Success(s) => complete(ToResponseMarshallable(repoes))
              case Failure(e) => logger.error("Error bulk index: " + e.getMessage)
                complete(ToResponseMarshallable(List.empty[GitRepo]))
            }
          case Failure(f) => logger.error("Error: " + f.getMessage)
            complete(ToResponseMarshallable(List.empty[GitRepo]))
        }
      }

    } ~ path("search") {
      get {
        parameters('user.as[String], 'title.as[String]) { (user, title) =>
          val ret = GitElasticService.find(("user", user), ("title", title))
          onComplete(ret) {
            case Success(s) => logger.info(s"""Success searching with user=${user} & title=${title}: ${s}""")
              complete(ToResponseMarshallable(s))
            case Failure(f) => logger.error(s"""Failed ${user}${user} & title=${title}: ${f}""")
              complete(ToResponseMarshallable(f))
          }
        } ~ parameters('user) { user =>
          if (user == "all") {
            val ret = GitElasticService.findAll()
            onComplete(ret) {
              case Success(s) => logger.info(s"""Success all: ${s}""")
                complete(ToResponseMarshallable(s))
              case Failure(f) => logger.error(s"""Failed all: ${f}""")
                complete(ToResponseMarshallable(f))
            }
          } else {
            val ret = GitElasticService.find(("user", user))
            onComplete(ret) {
              case Success(s) => logger.info(s"""Success searching with user=${user}: ${s}""")
                complete(ToResponseMarshallable(s))
              case Failure(f) => logger.error(s"""Failed user=${user}: ${f}""")
                complete(ToResponseMarshallable(List.empty[GitRepo]))
            }
          }
        }

      }
    }
  }

}

