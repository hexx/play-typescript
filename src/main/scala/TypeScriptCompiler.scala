package com.github.hexx

import java.io.File
import scala.io.Source
import scala.sys.process._
import scala.util.control.Exception._
import sbt.PlayExceptions.AssetCompilationException
import play.api._
import play.core.jscompile.JavascriptCompiler

object TypeScriptCompiler {
  def compile(file: File, options: Seq[String]) = {
    val js = executeNativeCompiler(file, options)
    (js, minify(js, file), Seq(file))
  }

  def minify(js: String, file: File) = {
    catching(classOf[AssetCompilationException]).opt(JavascriptCompiler.minify(js, Some(file.getName)))
  }

  def executeNativeCompiler(src: File, options: Seq[String]) = {
    val dir = new File(src.getParentFile.getAbsolutePath)
    val dest = File.createTempFile(src.getName, ".js")
    try {
      val process = Process(Seq("tsc", "--out", dest.getAbsolutePath) ++ options ++ Seq(src.getName), dir)
      var out = new StringBuilder
      var err = new StringBuilder
      val logger = ProcessLogger(s => out.append(s + "\n"), s => err.append(s + "\n"))
      val exit = process ! logger
      if (exit != 0) {
        val regex = """(?s).*ts *\(([0-9]+),([0-9]+)\).*""".r
        val regex(line, column) = err.mkString
        throw AssetCompilationException(Some(src), err.mkString, line.toInt, column.toInt)
      }
      Source.fromFile(dest).mkString
    } finally {
      dest.delete()
    }
  }
}
