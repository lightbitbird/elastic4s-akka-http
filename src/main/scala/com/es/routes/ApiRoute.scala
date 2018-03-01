package com.es.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import com.es.client.GithubClient
import com.es.models.{BaseEntity, GitRepo, JsonSupport}
import com.es.services.GitElasticService
import com.sksamuel.elastic4s.bulk.RichBulkResponse
import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContextExecutor, Future}
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
        parameters('user.as[String], 'category.as[String]) { (user, category) =>
          val ret = GitElasticService.findAll()
          onComplete(ret) {
            case Success(s) => logger.info(s"""Success with category: ${s}""")
              complete(ToResponseMarshallable(s))
            case Failure(f) => logger.error(s"""Failed ${user}, ${category}: ${f}""")
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
            val ret = GitElasticService.find(user, "")
            onComplete(ret) {
              case Success(s) => logger.info(s"""Success type: ${s}""")
                complete(ToResponseMarshallable(s))
              case Failure(f) => logger.error(s"""Failed type: ${f}""")
                complete(ToResponseMarshallable(List.empty[GitRepo]))
            }
          }
        }

      }
    }
  }

}

