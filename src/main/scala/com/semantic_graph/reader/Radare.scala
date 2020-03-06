package com.semantic_graph.reader

import com.semantic_graph.{EdgeData, EdgeType, NodeData, NodeType, Provenance, SemanticGraph}
import io.github.izgzhen.msbase.JsonUtil

class Block(size: Int, val ops: List[Map[String, Object]], colorize: Int, val offset: Int, val jump: Int, val fail: Int)

class SuperBlock(name: String, ninstr: Int, val blocks: List[Block],
                 `type`: String, offset: Int, nlocals: Int, nargs: Int, size: Int, stack: Int)

object Radare {
  def parseOpObj(opObj: Map[String, Object]) : String = {
    if (opObj("type") == "invalid") {
      "INVALID"
    } else {
      opObj("opcode").asInstanceOf[String]
    }
  }

  def parse(s: String) : SemanticGraph = {
    val parsedJson = JsonUtil.fromJSON[Map[String, List[SuperBlock]]](s)
    var blockMap : Map[Int, (Int, Int, List[String])] = Map()
    for ((_, blocks) <- parsedJson) {
      assert(blocks.size == 1)
      for (block <- blocks.head.blocks) {
        assert (!blockMap.contains(block.offset))
        blockMap += (block.offset -> (block.jump, block.fail, block.ops.map(parseOpObj)))
      }
    }
    val g = new SemanticGraph(Provenance.Unknown())
    for ((offset, (_, _, ops)) <- blockMap) {
      g.addNode(NodeData(ops, NodeType.UNKNOWN), offset)
    }
    if (!g.hasNode(0)) {
        g.addNode(NodeData(Seq(), NodeType.UNKNOWN), 0)
    }
    for ((offset, (jump, fail, _)) <- blockMap) {
      g.addEdge(offset, jump, EdgeData(EdgeType.ControlFlow))
      if (fail != jump) {
        g.addEdge(offset, fail, EdgeData(EdgeType.ControlFlow))
      }
    }
    g
  }
}
