package com.vjache.crawler.engine

import java.io.File
import java.net.URI
import java.util.concurrent._
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Predicate

/**
  * The Crawler.
  */
object Crawler extends Runnable{

  val queue      = new ConcurrentLinkedQueue[String]()
  val downloaded = new ConcurrentSkipListSet[String]()
  val domains    = new ConcurrentSkipListSet[String]()
  val exec       = initThreadPool()

  val paused     = new AtomicBoolean(false)

  val repo = new Repo(new File("./crawler-data/"))

  def matchDomainFilter(url: String): Boolean = {
    domains.stream().anyMatch(new Predicate[String]{
      override def test(domain: String): Boolean = {
        val host = new URI(url).getHost
        host.endsWith(domain)
      }
    })
  }

  private def initThreadPool(): ExecutorService = {
    val nThreads = Runtime.getRuntime.availableProcessors()
    new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new SynchronousQueue[Runnable]())
  }

  override def run(): Unit = {
    //domains.add("dustyfeet.com")
    //queue.add("http://www.dustyfeet.com/")
    while (true) {
      if (!paused.get()) {
        val url = queue.poll()
        if (url == null) {
          Thread.sleep(100)
        }
        else if (downloaded.contains(url)) {
          println(s"Already visited: $url")
        }
        else if (matchDomainFilter(url)) {
          exec.execute(new DownloadTask(url))
          Thread.sleep(100)
        }
        else {
          println(s"Not match domain: $url")
        }
      }
      else {
        Thread.sleep(100)
      }
    }
  }

  new Thread(Crawler).start()
}
