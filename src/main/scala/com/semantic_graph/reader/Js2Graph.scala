package com.semantic_graph.reader

import com.semantic_graph.{EdgeData, EdgeType, NodeData, NodeId, NodeType, SemanticGraph}
import io.github.izgzhen.msbase.JsonUtil

class JsGraph(val nodes: List[String], val edges: List[List[Int]])

object Js2Graph {
  def parse(s: String) : SemanticGraph = {
    val parsedJson = JsonUtil.fromJSON[JsGraph](s)
    val g = new SemanticGraph()
    for ((node, i) <- parsedJson.nodes.view.zipWithIndex) {
      g.addNode(NodeData(Seq(node), NodeType.UNKNOWN), NodeId(i.toString))
    }
    for (edges <- parsedJson.edges) {
      g.addEdge(NodeId(edges(0).toString), NodeId(edges(1).toString), EdgeData(EdgeType.ControlFlow))
    }
    g
  }
}
