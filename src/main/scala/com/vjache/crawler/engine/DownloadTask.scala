package com.vjache.crawler.engine

import java.net.{SocketTimeoutException, URI}

import com.vjache.crawler.engine.DownloadTask._

import scala.annotation.tailrec
import scalaj.http.{Http, HttpResponse}


object DownloadTask {
  val hrefRe = """<a [^<>]*href=['"]([^\'" ><]+)['"][^<]*>""".r
  val hrefRelRe = """^/.*""".r
  val hrefAbsRe = """^http.*""".r
  val absRe = "(^http.*)".r
  val relRe = "^/(.*)".r
  val relRe2 = "^./(.*)".r
  val relRe3 = "^../(.*)".r
  val emailRe = "(^.*@.*)".r
}

/**
  *
  */
class DownloadTask(val url: String) extends Runnable {

  override def run(): Unit = {
    val (pageContent, status) = downloadPage(url, 3)
    var cnt = 0
    if (status == 200) {
      val uri = new URI(url)
      val port = if (uri.getPort == -1) 80 else uri.getPort
      val baseURL = s"${uri.getScheme}://${uri.getHost}:$port/"
      val refs: Iterator[String] = parseRefs(baseURL, pageContent)
      refs.foreach({ ref =>
        cnt = cnt + 1
//        println(s"ADD-QUEUE: ${(url, ref)}")
        Crawler.queue.add(ref)
      })
      Crawler.repo.save(url, pageContent, status)
      Crawler.downloaded.add(url)
    }
    println(s"Downloaded (status = $status, refs=$cnt): $url")
  }

  def parseRefs(baseUrl: String, text0: String): Iterator[String] = {
    val text = text0.replaceAll("[\n\r]", "")
    hrefRe.findAllMatchIn(text).map(_ group 1).map({ ref: String =>
      ref match {
        case emailRe(_)  => null
        case absRe(_)    => ref
        case relRe(rel)  => baseUrl + rel
        case relRe2(rel) => baseUrl + rel
        case relRe3(rel) => baseUrl + rel
        case _           => baseUrl + ref
      }
    }).filter(_ != null)
  }

  @tailrec
  private final def downloadPage(url: String, attempts: Int): (String, Int) = {
    try {
      val response: HttpResponse[String] = Http(url).timeout(5000, 10000).asString
      println(s"HEADERS: ${response.headers}")
      response.cookies
      (response.body, response.code)
    }
    catch {
      case e: SocketTimeoutException =>
        if (attempts <= 0)
          throw new RuntimeException(s"All attempts failed to download page: $url", e)
        else {
          downloadPage(url, attempts - 1)
        }
      case e:Exception =>
        throw new RuntimeException(s"Failed to download page: $url", e)
    }
  }
}
