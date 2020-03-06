package com.semantic_graph.reader

import com.semantic_graph.{EdgeData, EdgeType, NodeData, NodeType, Provenance, SemanticGraph}
import io.github.izgzhen.msbase.JsonUtil

class ShGraph(val Nodes: List[List[String]], val Edges: List[List[Int]])

object Sh2Graph {
  def parse(s: String) : SemanticGraph = {
    val parsedJson = JsonUtil.fromJSON[ShGraph](s)
    val g = new SemanticGraph(Provenance.Unknown())
    for ((ops, i) <- parsedJson.Nodes.view.zipWithIndex) {
      g.addNode(NodeData(ops, NodeType.UNKNOWN), i)
    }
    if (parsedJson.Edges != null) {
      for (edges <- parsedJson.Edges) {
        g.addEdge(edges(0), edges(1), EdgeData(EdgeType.ControlFlow))
      }
    }
    g
  }
}
