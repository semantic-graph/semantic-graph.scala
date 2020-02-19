package com.semantic_graph.reader

import com.semantic_graph.{EdgeData, EdgeType, JsonUtil, NodeData, NodeType, SemanticGraph}

case class RbEdge(u: Int, v: Int, `type`: String)
case class RbGraph(nodes: List[String], edges: List[RbEdge])

object Rb2Graph {
  def parse(s: String) : SemanticGraph = {
    val parsedJson = JsonUtil.fromJSON[RbGraph](s)
    val g = new SemanticGraph()
    for ((node, i) <- parsedJson.nodes.view.zipWithIndex) {
      g.addNode(NodeData(Seq(node), NodeType.UNKNOWN), i)
    }
    for (e <- parsedJson.edges) {
      g.addEdge(e.u, e.v, EdgeData(e.`type` match {
        case "control" => EdgeType.ControlFlow
        case "data" => EdgeType.DataFlow
      }))
    }
    g
  }
}
