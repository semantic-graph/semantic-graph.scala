package com.semantic_graph.reader

import java.io.{File, FileInputStream}

import com.semantic_graph.{EdgeData, EdgeType, NodeData, NodeId, NodeType, SemanticGraph}

import scala.xml._

/* Created at 2/19/20 by zhen */
object Gexf2Graph {
  def parse(str: String): SemanticGraph = {
    val xmlFile = XML.load(new FileInputStream(new File(str)))
    val g = new SemanticGraph()
    for (node <- xmlFile \\ "node") {
      val label = (node \ "@label").text
      val id = (node \ "@id").text
      var `type` = NodeType.UNKNOWN
      for (attr <- node \\ "attvalue") {
        val k = (attr \ "@for").text
        val v = (attr \ "@value").text
        if (k == "type") {
          `type` = NodeType.withName(v)
        }
      }
      g.addNode(NodeData(List(label), `type`), NodeId(id))
    }
    for (edge <- xmlFile \\ "edge") {
      val srcId = (edge \ "@source").text
      val tgtId = (edge \ "@target").text
      var `type` = EdgeType.Unknown
      for (attr <- edge \\ "attvalue") {
        val k = (attr \ "@for").text
        val v = (attr \ "@value").text
        if (k == "type") {
          `type` = EdgeType.fromGexf(v)
        }
      }
      g.addEdge(NodeId(srcId), NodeId(tgtId), EdgeData(`type`))
    }
    g
  }
}
