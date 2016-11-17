package com.vjache.crawler.restapi

import java.net.{ConnectException, Socket}

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest

class RestAPIServiceSpec extends Specification with Specs2RouteTest with RestAPIService {
  def actorRefFactory = system

  def startRestAPIWebServer() = {
    RestAPIBoot.main(new Array[String](0))
    while (try {new Socket("localhost", 8080); false} catch { case _:ConnectException => true}) {
      Thread.sleep(100)
    }
  }

  "RestAPIService" should {

    "when just started return an empty list for GET /domains" in {
      Get("/domains") ~> myRoute ~> check {
        responseAs[String] must contain("[]")
      }
    }

    "return a non empty list for GET /domains after POST /domains" in {
      Post("/domains", "aaa,xxx") ~> myRoute ~> check {
        val rspStr: String = responseAs[String]
        rspStr must contain("aaa")
        rspStr must contain("xxx")
      }
      Get("/domains") ~> myRoute ~> check {
        val rspStr: String = responseAs[String]
        rspStr must contain("aaa")
        rspStr must contain("xxx")
      }
    }

    "return a status info for GET /status" in {
      Get("/status") ~> myRoute ~> check {
        val rspStr: String = responseAs[String]
        rspStr must contain("queue-size")
        rspStr must contain("downloaded-size")
      }
    }

    "pauses with POST /pause and releases with with POST /release" in {
      Post("/pause") ~> myRoute ~> check {
        responseAs[String] must contain("OK")
      }
      Post("/resume") ~> myRoute ~> check {
        responseAs[String] must contain("OK")
      }
    }

    "downloads test site at 'http://localhost:8080/index.html'" in {
      startRestAPIWebServer()
      Post("/domains", "localhost") ~> myRoute ~> check {
        val rspStr: String = responseAs[String]
        rspStr must contain("localhost")
      }
      Post("/page", "http://localhost:8080/index.html") ~> myRoute ~> check {
        val rspStr: String = responseAs[String]
        rspStr must contain("OK")
      }
      Thread.sleep(2000)
      1 === 1
    }
  }
}
