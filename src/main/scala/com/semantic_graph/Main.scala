package com.semantic_graph

import java.io.{BufferedWriter, File, FileWriter}

import com.semantic_graph.reader.Raw
import scopt.OParser

import scala.io.Source
import java.util.concurrent.ForkJoinPool
import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.ForkJoinTaskSupport

case class Config(out: File)

object Main {
  def main_(args: Array[String]): Unit = {
    val builder = OParser.builder[Config]
    val parser = {
      import builder._
      OParser.sequence(
        programName("semdiff"),
        head("semdiff", "0.x"),
        opt[File]('o', "out").required().valueName("<output>").action((x, c) => c.copy(out = x))
      )
    }
    val g1 = Raw.parse(args(0))
    var configStart = 2
    val outputGraph = if (args(1).startsWith("-")) {
      configStart = 1
      g1
    } else {
      val g2 = Raw.parse(args(1))
      g1.diff(g2)
    }
    val config = OParser.parse(parser, args.drop(configStart), Config(null)).get
    val bw = new BufferedWriter(new FileWriter(config.out))
    bw.write(outputGraph.toJSON)
    bw.close()
  }

  def main(args: Array[String]): Unit = {
    if (args(0) == "--bulk") {
      val argsFile = args(1)
      val src = Source.fromFile(argsFile)
      val parLines = src.getLines.toList.par
      parLines.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(4))
      parLines.foreach(line => main_(line.split(' ')))
      src.close()
    } else {
      main_(args)
    }
  }
}
