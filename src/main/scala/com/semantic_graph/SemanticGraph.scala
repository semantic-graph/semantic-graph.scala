package com.semantic_graph

import com.fasterxml.jackson.annotation.JsonValue
import com.semantic_graph.Provenance.{Subtract, Union}
import io.github.izgzhen.msbase.JsonUtil

case class NodeId(id: String) extends Comparable[NodeId] {
  @JsonValue
  def getId: String = id

  override def compareTo(o: NodeId): Int = id.compareTo(o.id)
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
class SemanticGraph (val provenance: Provenance) {
  protected var nodes : Map[NodeId, NodeData] = Map()
  protected var nodeHashMap : Map[Int, Set[NodeId]] = Map()
  protected var edges : Map[(NodeId, NodeId), EdgeData] = Map()
  protected var edgeHashMap : Map[Int, Set[(NodeId, NodeId)]] = Map()

  override def toString: String = {
    val es = edges.map { case ((u, v), edgeData) => (nodes(u).toString, nodes(v).toString, edgeData.toString) }.toList
    es.sortBy(_._1).map(x => x._1 + ", " + x._2 + ": " + x._3).mkString("\n")
  }

  def copy(newProvenance: Provenance) : SemanticGraph = {
    val g = new SemanticGraph(newProvenance)
    g.nodes ++= nodes
    g.nodeHashMap ++= nodeHashMap
    g.edges ++= edges
    g.edgeHashMap ++= edgeHashMap
    g
  }

  def subtract(other: SemanticGraph) : SemanticGraph = {
    val newProvenance = Subtract(provenance, other.provenance)
    val self = this.copy(newProvenance)
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
    for (k <- other.nodeHashMap.keys) {
      if (self.nodeHashMap.contains(k)) {
        for (n <- self.nodeHashMap(k)) {
          if (self.neighbors(n).isEmpty) {
            self.delNode(n)
          }
        }
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
    val newProvenance = Union(provenance, other.provenance)
    val g = other.copy(newProvenance)
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
    edges = edges.removedAll(edgeHashMap(k))
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
    assert(!edges.contains((u, v)), edges.get((u, v)))
    edges += ((u, v) -> edgeData)
    val edgeHash = (nodes(u).valHash(), nodes(v).valHash(), edges((u, v)).valHash()).hashCode()
    val prevSet = edgeHashMap.getOrElse(edgeHash, Set())
    val newEdge = (u, v)
    edgeHashMap += (edgeHash -> prevSet.+(newEdge))
  }

  def hasEdge(u: NodeId, v: NodeId) : Boolean = {
    edges.contains((u, v))
  }

  def iterNodes : Iterator[(NodeId, NodeData)] = nodes.iterator
  def iterEdges : Iterator[((NodeId, NodeId), EdgeData)] = edges.iterator

  def toJSON: String = {
    JsonUtil.toJson(Map[String, Object](
      "nodes" -> nodes,
      "edges" -> edges.toList,
      "provenance" -> provenance.toString
    ))
  }

  def addNode(nodeData: NodeData, nodeId: Int) : NodeId = {
    addNode(nodeData, NodeId(nodeId.toString))
  }

  def addNode(nodeData: NodeData, nodeId: NodeId) : NodeId = {
    assert (!nodes.contains(nodeId))
    nodes += (nodeId -> nodeData)
    val hash = nodeData.valHash()
    val nodeSet = nodeHashMap.getOrElse(nodeData.valHash(), Set())
    nodeHashMap += (hash -> nodeSet.+(nodeId))
    nodeId
  }

  def hasNode(nodeId: Int) : Boolean = hasNode(NodeId(nodeId.toString))

  def hasNode(nodeId: NodeId) : Boolean = {
    nodes.contains(nodeId)
  }
}

class SelfNamedGraph(override val provenance: Provenance) extends SemanticGraph(provenance) {
  def addNode(nodeData: NodeData) : NodeId = {
    addNode(nodeData, NodeId(nodes.size.toString))
  }
}
