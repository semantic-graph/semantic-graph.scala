package com.semantic_graph.writer

import com.semantic_graph.NodeId

/* Created at 2/19/20 by zhen */
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
