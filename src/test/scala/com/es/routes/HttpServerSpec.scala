package com.es.routes

import akka.http.scaladsl.server.Route
import com.es.BaseServiceTest
import com.es.models.{GitRepo, Owner}

class HttpServerSpec extends BaseServiceTest {

  "HttpServer" when {
    "GET /api/search?user=all" should {
      "return 200 OK" in {
        Get("/api/search?user=all") ~> new HttpServer().route ~> check {
          //implicit def um: FromEntityUnmarshaller[List[GitRepo]] =
          //  Unmarshaller.stringUnmarshaller.forContentTypes(ContentTypes.`application/json`).map { str =>
          //}

          val list = List(GitRepo(121942118, "elastic4s-akka-http", Owner(14951865, "lightbitbird", "https://api.github.com/users/lightbitbird"), "https://api.github.com/repos/lightbitbird/elastic4s-akka-http", "Scala"),
            GitRepo(114505945, "reactive-kafka-app", Owner(14951865, "lightbitbird", "https://api.github.com/users/lightbitbird"), "https://api.github.com/repos/lightbitbird/reactive-kafka-app", "Scala"),
            GitRepo(108731072, "reactive-twitter-stream-withslick", Owner(14951865, "lightbitbird", "https://api.github.com/users/lightbitbird"), "https://api.github.com/repos/lightbitbird/reactive-twitter-stream-withslick", "Scala"))

          responseAs[List[GitRepo]] shouldBe list
          status.intValue() shouldBe 200
        }
      }
    }
  }

}
