package com.es

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.es.models.JsonSupport
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, MustMatchers, WordSpec}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

trait BaseServiceTest extends WordSpec with Matchers
  with ScalatestRouteTest with MockitoSugar with JsonSupport {

  def awaitForResult[T](futureResult: Future[T]): T =
    Await.result(futureResult, 5.seconds)

}
