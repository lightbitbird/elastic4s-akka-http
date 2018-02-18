package com.es.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.es.config.ActorSystemConfig
import com.es.models._

import scala.concurrent.{ExecutionContext, Future}

abstract class RestClient[R <: BaseResult[T, A], T <: BaseEntity[A], A](implicit actorSystem: ActorSystem,
                                                                        materializer: ActorMaterializer,
                                                                        ec: ExecutionContext) extends JsonSupport {

  def getResponse(user: String): Future[HttpResponse] = {
    //pattern1:
    //val uri = Uri("https://api.github.com/search/repositories?q=akka+user:" + user)
    //val request: HttpRequest = HttpRequest(method = HttpMethods.GET, uri)
    //val res = Http().singleRequest(request)

    //pattern2:
    val uri = Uri("/search/repositories?q=akka+user:" + user)
    val request: HttpRequest = HttpRequest(method = HttpMethods.GET, uri)
    val connectionFlow = Http().outgoingConnectionHttps("api.github.com")
    val response = Source.single(request).via(connectionFlow).runWith(Sink.head)

    response
  }

  def getResources(user: String): Future[List[T]]

}

class GithubClient(implicit actorSystem: ActorSystem,
                   materializer: ActorMaterializer,
                   ec: ExecutionContext) extends RestClient[GitResult, GitRepo, Long] {
  override def getResources(user: String): Future[List[GitRepo]] = {
    val response = this.getResponse(user)
    //pattern1:
    response.flatMap(res => Unmarshal(res.entity).to[GitResult].map(_.items))

    //pattern2:
    //response.flatMap(_.entity.dataBytes.runFold(ByteString.empty)(_ ++ _)
    // .flatMap(bs => Unmarshal(bs.utf8String).to[Result].map(_.items)))
  }
}

