package com.es.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.{Directives, Route}
import akka.stream.ActorMaterializer
import com.es.client.GithubClient
import com.es.models.{BaseEntity, GitRepo, JsonSupport}
import com.es.services.GitElasticService
import com.sksamuel.elastic4s.bulk.RichBulkResponse
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
        completeOrRecoverWith(items) {
          case e: Exception =>
            logger.error("Failed to get Git resources: " + e.getMessage)
            complete(ToResponseMarshallable(List.empty[GitRepo]))
        } ~ onComplete(items) {
          case Success(repoes) =>
            val bulk = GitElasticService.indexBulk(repoes)
            onSuccess(bulk) {
              case s: RichBulkResponse => complete(ToResponseMarshallable(items))
              case e: Exception =>
                logger.error("Error: " + e.getMessage)
                complete(ToResponseMarshallable(List.empty[GitRepo]))
            }
          case Failure(f) =>
            logger.error("Error: " + f.getMessage)
            complete(ToResponseMarshallable(List.empty[GitRepo]))
        }
      }

    } ~ path("search") {
      get {
        parameters('user.as[String], 'category.as[String]) { (user, category) =>
          val ret = GitElasticService.findAll()
          onSuccess(ret) {
            s => complete(ToResponseMarshallable(s))

          } ~ completeOrRecoverWith(ret) {
            case e =>
              logger.error("Error: " + e.getMessage)
              complete(ToResponseMarshallable(List.empty[GitRepo]))
          }
        } ~ parameters('user) { user =>
          if (user == "all") {
            val ret = GitElasticService.findAll()
            onSuccess(ret) {
              s => complete(ToResponseMarshallable(s))

            } ~ completeOrRecoverWith(ret) {
              case e =>
                logger.error("Error: " + e.getMessage)
                complete(ToResponseMarshallable(List.empty[GitRepo]))
            }
          } else {
            val ret = GitElasticService.find(user, "")
            onSuccess(ret) {
              s => complete(ToResponseMarshallable(s))

            } ~ completeOrRecoverWith(ret) {
              case e =>
                logger.error("Error: " + e.getMessage)
                complete(ToResponseMarshallable(List.empty[GitRepo]))
            }
          }
        }

      }
    }
  }

}

