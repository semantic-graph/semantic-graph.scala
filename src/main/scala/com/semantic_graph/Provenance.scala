package com.semantic_graph

/* Created at 3/6/20 by zhen */
sealed abstract class Provenance extends Product with Serializable {
  def toJSONObject: Object
}

object Provenance {
  final case class Unknown() extends Provenance {
    override def toJSONObject: Object = Map("tag" -> "Unknown")
  }
  final case class Gexf(input: String) extends Provenance {
    override def toJSONObject: Object = Map("tag" -> "Gexf", "input" -> input)
  }
  final case class Js(input: String) extends Provenance {
    override def toJSONObject: Object = Map("tag" -> "Js", "input" -> input)
  }
  final case class Subtract(g1: Provenance, g2: Provenance) extends Provenance {
    override def toJSONObject: Object = Map("tag" -> "Subtract", "g1" -> g1.toJSONObject, "g2" -> g2.toJSONObject)
  }
  final case class Union(g1: Provenance, g2: Provenance) extends Provenance {
    override def toJSONObject: Object = Map("tag" -> "Union", "g1" -> g1.toJSONObject, "g2" -> g2.toJSONObject)
  }
}