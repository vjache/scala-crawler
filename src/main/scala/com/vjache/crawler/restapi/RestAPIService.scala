package com.vjache.crawler.restapi

import com.vjache.crawler.engine.Crawler
import org.json4s._
import spray.http.MediaTypes._
import spray.http.StatusCodes.{Accepted, Created}
import spray.httpx.Json4sSupport
import spray.routing._

object Json4sProtocol extends Json4sSupport {
  implicit def json4sFormats: Formats = DefaultFormats
}


// this trait defines our service behavior independently from the service actor
trait RestAPIService extends HttpService {

  val myRoute =
    path("domains") {
      get {
        respondWithMediaType(`text/plain`) {
          // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            s"${Crawler.domains}"
          }
        }
      } ~
        post {
          respondWithStatus(Created) {
            entity(as[String]) { someObject =>
              complete {
                Crawler.domains.clear()
                someObject.split(",").map({s=>s.trim}).foreach({s=>Crawler.domains.add(s)})
                s"${Crawler.domains}"
              }
            }
          }
        }
    } ~ path("status") {
      get {
        respondWithMediaType(`text/plain`) {
          // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            s"queue-size=${Crawler.queue.size()}, downloaded-size=${Crawler.downloaded.size()}"
          }
        }
      }
    } ~ path("page") {
        post {
          respondWithStatus(Created) {
            entity(as[String]) { someObject =>
              complete {
                Crawler.queue.add(someObject)
                s"OK"
              }
            }
          }
        }
    } ~ path("pause") {
      post {
        respondWithStatus(Accepted) {
          entity(as[String]) { someObject =>
            complete {
              Crawler.paused.set(true)
              s"OK"
            }
          }
        }
      }
    } ~ path("resume") {
      post {
        respondWithStatus(Accepted) {
          entity(as[String]) { someObject =>
            complete {
              Crawler.paused.set(false)
              s"OK"
            }
          }
        }
      }
    } ~ pathPrefix("css") {
      get { getFromResourceDirectory("web/css") }
    } ~ pathPrefix("js") {
      get { getFromResourceDirectory("web/js") }
    } ~ pathPrefix("") {
      get { getFromResourceDirectory("web") }
    }
}