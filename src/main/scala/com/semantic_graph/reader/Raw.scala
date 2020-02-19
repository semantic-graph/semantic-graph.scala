package com.semantic_graph.reader

import java.nio.file.Paths

import com.semantic_graph.SemanticGraph
import guru.nidi.graphviz.parse.Parser

import scala.io.Source

object Raw {
  def parse(path: String) : SemanticGraph = {
    val s = Source.fromFile(path)
    val str = s.mkString
    s.close()
    val p = Paths.get(path)
    // expect path to ends with json
    val fileName : String = p.getFileName.toString
    val segments = fileName.split('.').reverse
    assert(segments.length >= 3)
    segments(0) match {
      case "json" => {
        segments(1) match {
          case "radare" => Radare.parse(str)
          case "js2graph" => Js2Graph.parse(str)
          case "py2graph" => Py2Graph.parse(str)
          case "sh2graph" => Sh2Graph.parse(str)
          case "rb2graph" => Rb2Graph.parse(str)
          case suffix => throw new RuntimeException("Unknown suffix: " + suffix)
        }
      }
      case "dot" => {
        val p = new Parser()
        val g = p.read(str)
        Dot2Graph.parse(g)
      }
      case ext => throw new RuntimeException("Unknown extension: " + ext)
    }
  }
}
