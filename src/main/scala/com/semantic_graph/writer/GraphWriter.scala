package com.semantic_graph.writer

import com.semantic_graph.NodeId
import scala.jdk.CollectionConverters._

/* Created at 2/19/20 by zhen */
trait GraphWriter[NodeAttr <: Serializable, EdgeAttr <: Serializable]
  extends Serializable {
  def createNode(label: String, attrs: Map[NodeAttr, String]): NodeId
  def createNode(label: String, attrs: java.util.Map[NodeAttr, String]): NodeId =
    createNode(label, attrs.asScala.toMap)
  def getNodes: Set[NodeId]
  def getNodeLabel(nodeId: NodeId): String
  def getNodeAttrs(nodeId: NodeId): Map[NodeAttr, String]
  def getEdges: Set[(NodeId, NodeId)]
  def getEdgeAttrs(srcId: NodeId, tgtId: NodeId): Map[EdgeAttr, String]
  def addEdge(from: NodeId, to: NodeId, attrs: Map[EdgeAttr, String]): Unit
  def addEdge(from: NodeId, to: NodeId, attrs: java.util.Map[EdgeAttr, String]): Unit =
    addEdge(from, to, attrs.asScala.toMap)
  def write(path: String): Unit
}
