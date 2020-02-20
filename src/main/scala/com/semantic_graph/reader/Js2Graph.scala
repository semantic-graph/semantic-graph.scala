package com.semantic_graph.reader

import com.semantic_graph.{EdgeData, EdgeType, JsonUtil, NodeData, NodeId, NodeType, SemanticGraph}

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
