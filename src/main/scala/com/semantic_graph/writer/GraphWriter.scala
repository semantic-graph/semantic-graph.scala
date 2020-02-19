package com.semantic_graph.writer

/* Created at 2/19/20 by zhen */
case class NodeId(id: String)

trait GraphWriter[NodeAttr <: Serializable, EdgeAttr <: Serializable]
  extends Serializable {
  def createNode(label: String, attrs: Map[NodeAttr, String]): NodeId
  def getNodes: Set[NodeId]
  def getNodeLabel(nodeId: NodeId): String
  def getEdges: Set[(NodeId, NodeId)]
  def getEdgeAttrs(srcId: NodeId, tgtId: NodeId): Map[EdgeAttr, String]
  def addEdge(from: NodeId, to: NodeId, attrs: Map[EdgeAttr, String]): Unit
  def write(path: String): Unit
}
