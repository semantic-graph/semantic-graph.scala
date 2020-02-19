package com.semantic_graph.reader

import com.semantic_graph.{EdgeData, EdgeType, JsonUtil, NodeData, NodeType, SemanticGraph}

class PyGraph(val nodes: List[String], val edges: List[List[Int]])

object Py2Graph {
  def parse(s: String) : SemanticGraph = {
    val parsedJson = JsonUtil.fromJSON[PyGraph](s)
    val g = new SemanticGraph()
    for ((node, i) <- parsedJson.nodes.view.zipWithIndex) {
      g.addNode(NodeData(Seq(node), NodeType.UNKNOWN), i)
    }
    for (edges <- parsedJson.edges) {
      if (g.hasEdge(edges(0), edges(1))) {
        g.addEdge(edges(0), edges(1), EdgeData(EdgeType.ControlFlow))
      }
    }
    g
  }
}
