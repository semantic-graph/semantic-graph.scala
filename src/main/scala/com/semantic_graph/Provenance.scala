package com.semantic_graph

/* Created at 3/6/20 by zhen */
sealed abstract class Provenance extends Product with Serializable

object Provenance {
  final case class Unknown() extends Provenance
  final case class Js(input: String) extends Provenance
  final case class Subtract(g1: Provenance, g2: Provenance) extends Provenance
  final case class Union(g1: Provenance, g2: Provenance) extends Provenance
}
