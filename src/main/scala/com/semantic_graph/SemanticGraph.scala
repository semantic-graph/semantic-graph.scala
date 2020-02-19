package com.semantic_graph


object EdgeType extends Enumeration {
  val DataFlow, ControlFlow, Call = Value
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
  protected var nodeNames: List[String] = List()
  protected var nodes : Map[Int, NodeData] = Map()
  protected var nodeHashMap : Map[Int, Int] = Map()
  protected var edges : Map[(Int, Int), EdgeData] = Map()
  protected var edgeHashMap : Map[Int, (Int, Int)] = Map()

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

  def hasNodeName(nodeName: String) : Boolean = {
    nodeNames.contains(nodeName)
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

  def delNode(n: Int) : Unit = {
    nodeHashMap -= nodes(n).valHash()
    nodes -= n
  }

  def neighbors(node: Int) : Set[Int] = {
    (edges.keys map { case (u, v) =>
      if (u == node) { Some(v) }
      else if (v == node) { Some(u) }
      else None
    } filterNot (_.isEmpty) map (_.get)).toSet
  }

  def addEdge(u: Int, v: Int, edgeData: EdgeData) : Unit = {
    assert(edges.get((u, v)).isEmpty, edges.get((u, v)))
    edges += ((u, v) -> edgeData)
    edgeHashMap += ((nodes(u).valHash(), nodes(v).valHash(), edges((u, v)).valHash()).hashCode() -> (u, v))
  }

  def addEdge(u: String, v: String, edgeData: EdgeData) : Unit = {
    val uIdx = nodeNames.indexOf(u)
    val vIdx = nodeNames.indexOf(v)
    assert(vIdx != -1)
    assert(uIdx != -1)
    addEdge(uIdx, vIdx, edgeData)
  }

  def hasEdge(u: Int, v: Int) : Boolean = {
    edges.get((u, v)).isDefined
  }

  def iterNodes : Iterator[(Int, NodeData)] = nodes.iterator
  def iterEdges : Iterator[((Int, Int), EdgeData)] = edges.iterator

  def toJSON: String = {
    JsonUtil.toJson(Map[String, Object](
      "nodes" -> nodes,
      "edges" -> edges.toList
    ))
  }

  def addNode(nodeData: NodeData, nodeId: Int) : Int = {
    assert (!nodes.contains(nodeId))
    nodes += (nodeId -> nodeData)
    nodeHashMap += (nodeData.valHash() -> nodeId)
    nodeId
  }

  def addNode(nodeData: NodeData, nodeName: String) : Int = {
    val i = nodeNames.indexOf(nodeName)
    val nodeId = if (i == -1) {
      nodeNames :+= nodeName
      nodeNames.length - 1
    } else {
      i
    }
    addNode(nodeData, nodeId)
    nodeId
  }

  def hasNode(nodeId: Int) : Boolean = {
    nodes.contains(nodeId)
  }
}

class SelfNamedGraph extends SemanticGraph {
  def addNode(nodeData: NodeData) : Int = {
    addNode(nodeData, nodes.size.toString)
  }
}
