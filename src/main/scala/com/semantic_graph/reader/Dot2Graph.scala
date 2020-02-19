package com.semantic_graph.reader

import com.semantic_graph.{EdgeData, EdgeType, NodeData, NodeType, SemanticGraph}
import guru.nidi.graphviz.attribute.{Attributes, ForNode}
import guru.nidi.graphviz.model.{Link, MutableGraph, PortNode}

import scala.jdk.CollectionConverters._

object Dot2Graph {
  def parseNodeData(nodeName: String, node: Attributes[ForNode]): NodeData = {
    NodeData(operations = List(nodeName), `type` = {
      node.get("type") match {
        case null => NodeType.UNKNOWN
        case "STMT" => NodeType.STMT
        case "EXPR" => NodeType.EXPR
        case "CONSTANT" => NodeType.CONSTANT
        case "METHOD" => NodeType.METHOD
      }
    })
  }

  def parseEdgeData(link: Link): EdgeData = {
    EdgeData(`type` = {
      link.get("type") match {
        case "DATAFLOW" => EdgeType.DataFlow
        case "CONTROLFLOW" => EdgeType.ControlFlow
        case "CALL" => EdgeType.Call
      }
    })
  }

  // TODO: rename to integer indexing
  def parse(dotGraph: MutableGraph) : SemanticGraph = {
    val g = new SemanticGraph()
    for (node <- dotGraph.nodes.asScala) {
      val nodeName = node.name.toString
      if (!g.hasNodeName(nodeName)) {
        g.addNode(parseNodeData(nodeName, node), nodeName)
      }
      for (link <- node.links.asScala) {
        link.to match {
          case toNode:PortNode => {
            val toNodeName = toNode.name.toString
            if (!g.hasNodeName(toNodeName)) {
              g.addNode(parseNodeData(toNodeName, toNode.node()), toNodeName)
            }
            g.addEdge(nodeName, toNodeName, parseEdgeData(link))
          }
        }
      }
    }
    g
  }
}
