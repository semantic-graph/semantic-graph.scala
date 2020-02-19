import com.semantic_graph.reader.{Radare, Rb2Graph}
import com.semantic_graph.{EdgeData, EdgeType, NodeData, NodeType, SelfNamedGraph}
import junit.framework.TestCase
import org.junit.Assert._
import org.junit.Test

import scala.io.Source

class TestGraph extends TestCase {
  @Test def testGraph(): Unit = {
    val g = new SelfNamedGraph()
    val n1 = g.addNode(NodeData(Seq(), NodeType.UNKNOWN))
    val n2 = g.addNode(NodeData(Seq(), NodeType.UNKNOWN))
    g.addEdge(n1, n2, EdgeData(EdgeType.ControlFlow))
    for ((nodeId, nodeData) <- g.iterNodes) {
      println(nodeId + ": " + nodeData)
    }

    val h = g.diff(g.copy())
    assertEquals(h.iterNodes.toString(), 0, h.iterNodes.size)

    val h2 = new SelfNamedGraph()
    h2.addNode(NodeData(Seq("hello"), NodeType.UNKNOWN))
    val d2 = h.diff(h2)
    assertEquals(d2.iterNodes.toString(), 1, d2.iterNodes.size)
  }

  @Test def testDiffSame(): Unit = {
    val f1 = Source.fromFile("src/test/resources/strength_checker-0.0.1.rb.rb2graph.json")
    val f2 = Source.fromFile("src/test/resources/strength_checker-0.0.2.rb.rb2graph.json")
    val g1 = Rb2Graph.parse(f1.mkString)
    val g2 = Rb2Graph.parse(f2.mkString)
    f1.close()
    f2.close()
    val g = g1.diff(g2)
    assertEquals(g.iterNodes.toString(), 0, g.iterNodes.size)
  }

  @Test def testRadareReader(): Unit = {
    val source = Source.fromFile("src/test/resources/hello.blocks.json")
    val expectedJson = Source.fromFile("src/test/resources/hello.graph.json")
    val g = Radare.parse(source.mkString)
    val gJsonExpected = expectedJson.mkString
    source.close()
    expectedJson.close()
    assertEquals(28, g.iterNodes.size)
    assertEquals(40, g.iterEdges.size)
    val json = g.toJSON
    assertEquals(json, gJsonExpected, json)
  }

  @Test def testHash(): Unit = {
    val nd1 = NodeData(Seq("x", "y"), NodeType.UNKNOWN)
    val nd2 = NodeData(Seq("x", "y"), NodeType.UNKNOWN)
    assertEquals(nd1.valHash(), nd2.valHash())

    val e1 = EdgeData(EdgeType.ControlFlow)
    val e2 = EdgeData(EdgeType.ControlFlow)
    assertEquals(e1.valHash(), e2.valHash())

    val t1 = (nd1.valHash(), e1.valHash())
    val t2 = (nd2.valHash(), e2.valHash())
    assertEquals(t1.hashCode(), t2.hashCode())
  }
}
