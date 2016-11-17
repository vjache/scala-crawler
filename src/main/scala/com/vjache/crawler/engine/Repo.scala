package com.vjache.crawler.engine

import java.io.{File, PrintWriter}

/**
  * The Repository of downloaded content.
  */
class Repo(val baseDir:File) {

  def save(uri:String, text:String, status:Int): Unit = {
    val bucket = Math.abs(uri.hashCode % 256)
    val bucketDir = new File(baseDir, s"$bucket")
    bucketDir.mkdirs()
    val name = s"${Thread.currentThread().getId}_${System.currentTimeMillis()}"
    if (status == 200)
      writeFile(new File(bucketDir, s"$name.content"), text)
    writeFile(new File(bucketDir, s"$name.$status.url"), uri)
  }

  def writeFile (f:File, text:String): Unit = {
    var pw:PrintWriter = null
    try {
      pw = new PrintWriter(f)
      pw.write(text)
    }
    finally {
      if(pw != null) pw.close()
    }
  }
}
