package com.semantic_graph.reader

import com.semantic_graph.{EdgeData, EdgeType, JsonUtil, NodeData, NodeId, NodeType, SemanticGraph}

class PyGraph(val nodes: List[String], val edges: List[List[Int]])

object Py2Graph {
  def parse(s: String) : SemanticGraph = {
    val parsedJson = JsonUtil.fromJSON[PyGraph](s)
    val g = new SemanticGraph()
    for ((node, i) <- parsedJson.nodes.view.zipWithIndex) {
      g.addNode(NodeData(Seq(node), NodeType.UNKNOWN), NodeId(i.toString))
    }
    for (edges <- parsedJson.edges) {
      val u = NodeId(edges(0).toString)
      val v = NodeId(edges(1).toString)
      if (g.hasEdge(u, v)) {
        g.addEdge(u, v, EdgeData(EdgeType.ControlFlow))
      }
    }
    g
  }
}
