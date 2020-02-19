package com.semantic_graph.writer

import java.io.{File, FileWriter}

import it.uniroma1.dis.wsngroup.gexf4j.core.{EdgeType, Mode}
import it.uniroma1.dis.wsngroup.gexf4j.core.data.{Attribute, AttributeClass, AttributeType}
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.{GexfImpl, StaxGraphWriter}
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl

import scala.jdk.CollectionConverters._
import scala.collection.mutable

/* Created at 2/19/20 by zhen */
class GexfWriter[NodeAttr <: Serializable, EdgeAttr <: Serializable] extends GraphWriter[NodeAttr, EdgeAttr] {
  private val gexf = new GexfImpl()
  private val graph = gexf.getGraph
  graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC)
  private val nodeAttrs = new AttributeListImpl(AttributeClass.NODE)
  graph.getAttributeLists.add(nodeAttrs)
  private val edgeAttrs = new AttributeListImpl(AttributeClass.EDGE)
  graph.getAttributeLists.add(edgeAttrs)
  private val nodeAttrMap = mutable.Map[NodeAttr, Attribute]()
  private val edgeAttrMap = mutable.Map[EdgeAttr, Attribute]()
  private val edgeAttrRevMap = mutable.Map[Attribute, EdgeAttr]()

  private def getNodeAttr(attr: NodeAttr): Attribute = {
    if (!nodeAttrMap.contains(attr)) {
      nodeAttrMap.put(attr, nodeAttrs.createAttribute(attr.toString, AttributeType.STRING, attr.toString))
    }
    nodeAttrMap(attr)
  }

  private def getEdgeAttr(attr: EdgeAttr): Attribute = {
    if (!edgeAttrMap.contains(attr)) {
      val attrVal = edgeAttrs.createAttribute(attr.toString, AttributeType.STRING, attr.toString)
      edgeAttrMap.put(attr, attrVal)
      edgeAttrRevMap.put(attrVal, attr)
    }
    edgeAttrMap(attr)
  }

  override def createNode(label: String, attrs: Map[NodeAttr, String]): NodeId = {
    val node = graph.createNode()
    node.setLabel(label)
    for ((attr, value) <- attrs) {
      node.getAttributeValues.addValue(getNodeAttr(attr), value)
    }
    NodeId(node.getId)
  }

  override def getNodes: Set[NodeId] = graph.getNodes.asScala.map(n => NodeId(n.getId)).toSet

  override def getEdges: Set[(NodeId, NodeId)] =
    graph.getAllEdges.asScala.map(edge => (NodeId(edge.getSource.getId), NodeId(edge.getTarget.getId))).toSet

  override def getEdgeAttrs(srcId: NodeId, tgtId: NodeId): Map[EdgeAttr, String] = {
    for (edge <- graph.getNode(srcId.id).getEdges.asScala) {
      if (edge.getTarget.getId == tgtId.id) {
        return edge.getAttributeValues.asScala.map(v => (edgeAttrRevMap(v.getAttribute), v.getValue)).toMap
      }
    }
    throw new RuntimeException(s"Non-existent edge: $srcId, $tgtId")
  }

  override def addEdge(from: NodeId, to: NodeId, attrs: Map[EdgeAttr, String]): Unit = {
    val edge = graph.getNode(from.id).connectTo(graph.getNode(to.id))
    for ((attr, value) <- attrs) {
      edge.getAttributeValues.addValue(getEdgeAttr(attr), value)
    }
  }

  def write(path: String): Unit = {
    val graphWriter = new StaxGraphWriter()
    val f = new File(path)
    val out = new FileWriter(f, false)
    graphWriter.writeToStream(gexf, out, "UTF-8")
  }

  override def getNodeLabel(nodeId: NodeId): String = {
    graph.getNode(nodeId.id).getLabel
  }
}