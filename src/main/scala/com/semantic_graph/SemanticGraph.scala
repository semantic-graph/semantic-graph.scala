package com.semantic_graph

import com.fasterxml.jackson.annotation.JsonValue

case class NodeId(id: String) {
  @JsonValue
  def getId: String = id
}

object EdgeType extends Enumeration {
  val DataFlow, ControlFlow, Call, Unknown = Value

  def fromGexf(v: String): EdgeType.Value = {
    v match {
      case "DATAFLOW" => DataFlow
      case "CALL" => Call
    }
  }
}

object NodeType extends Enumeration {
  val EXPR, STMT, UNKNOWN, CONSTANT, METHOD = Value
}

case class NodeData(operations : Seq[String], `type`: NodeType.Value) {
  def valHash(): Int = operations.hashCode()
}
case class EdgeData(`type` : EdgeType.Value) {
  def valHash(): Int = `type`.hashCode()
}

// TODO: Use Scala-graph to rewrite this
class SemanticGraph {
  protected var nodes : Map[NodeId, NodeData] = Map()
  protected var nodeHashMap : Map[Int, NodeId] = Map()
  protected var edges : Map[(NodeId, NodeId), EdgeData] = Map()
  protected var edgeHashMap : Map[Int, (NodeId, NodeId)] = Map()

  def copy() : SemanticGraph = {
    val g = new SemanticGraph()
    g.nodes ++= nodes
    g.nodeHashMap ++= nodeHashMap
    g.edges ++= edges
    g.edgeHashMap ++= edgeHashMap
    g
  }

  def subtract(other: SemanticGraph) : SemanticGraph = {
    val self = this.copy()
    for (k <- other.edgeHashMap.keys) {
      if (self.edgeHashMap.contains(k)) {
        self.delEdge(k)
      }
    }
    for (n <- self.nodes.keys) {
      if (self.neighbors(n).isEmpty && neighbors(n).nonEmpty) {
        self.delNode(n)
      }
    }
    self
  }

  def diff(other: SemanticGraph) : SemanticGraph = {
    val g1 = subtract(other)
    val g2 = other.subtract(this)
    g1.union(g2)
  }

  def union(other: SemanticGraph) : SemanticGraph = {
    val g = other.copy()
    for ((nodeId, nodeData) <- nodes) {
      if (!g.nodes.contains(nodeId)) {
        g.addNode(nodeData, nodeId)
      }
    }
    for (((u, v), edgeData) <- edges) {
      if (!g.edges.contains((u, v))) {
        g.addEdge(u, v, edgeData)
      }
    }
    g
  }

  def delEdge(k: Int) : Unit = {
    edges -= edgeHashMap(k)
    edgeHashMap -= k
  }

  def delNode(n: NodeId) : Unit = {
    nodeHashMap -= nodes(n).valHash()
    nodes -= n
  }

  def neighbors(node: NodeId) : Set[NodeId] = {
    (edges.keys map { case (u, v) =>
      if (u == node) { Some(v) }
      else if (v == node) { Some(u) }
      else None
    } filterNot (_.isEmpty) map (_.get)).toSet
  }

  def addEdge(u: Int, v: Int, edgeData: EdgeData) : Unit = {
    addEdge(NodeId(u.toString), NodeId(v.toString), edgeData)
  }

  def addEdge(u: NodeId, v: NodeId, edgeData: EdgeData) : Unit = {
    assert(edges.get((u, v)).isEmpty, edges.get((u, v)))
    edges += ((u, v) -> edgeData)
    edgeHashMap += ((nodes(u).valHash(), nodes(v).valHash(), edges((u, v)).valHash()).hashCode() -> (u, v))
  }

  def hasEdge(u: NodeId, v: NodeId) : Boolean = {
    edges.get((u, v)).isDefined
  }

  def iterNodes : Iterator[(NodeId, NodeData)] = nodes.iterator
  def iterEdges : Iterator[((NodeId, NodeId), EdgeData)] = edges.iterator

  def toJSON: String = {
    JsonUtil.toJson(Map[String, Object](
      "nodes" -> nodes,
      "edges" -> edges.toList
    ))
  }

  def addNode(nodeData: NodeData, nodeId: Int) : NodeId = {
    addNode(nodeData, NodeId(nodeId.toString))
  }

  def addNode(nodeData: NodeData, nodeId: NodeId) : NodeId = {
    assert (!nodes.contains(nodeId))
    nodes += (nodeId -> nodeData)
    nodeHashMap += (nodeData.valHash() -> nodeId)
    nodeId
  }

  def hasNode(nodeId: Int) : Boolean = hasNode(NodeId(nodeId.toString))

  def hasNode(nodeId: NodeId) : Boolean = {
    nodes.contains(nodeId)
  }
}

class SelfNamedGraph extends SemanticGraph {
  def addNode(nodeData: NodeData) : NodeId = {
    addNode(nodeData, NodeId(nodes.size.toString))
  }
}
