package dotty.xml.interpolator

extension nodeOps on (self: scala.xml.Node) with

  def ≈ (that: scala.xml.Node): Boolean =
    def hasSameScope(self: scala.xml.Node, that: scala.xml.Node): Boolean =
      self.scope == that.scope
      && {
        val zipped = (self, that) match
          case (g1: scala.xml.Group, g2: scala.xml.Group) => (g1.nodes, g2.nodes).zipped
          case (n1, n2) => (n1.child, n2.child).zipped
        zipped.forall(hasSameScope)
      }
    self == that && hasSameScope(self, that)

  def !≈ (that: scala.xml.Node): Boolean = !(self ≈ that)


extension nodeBufferOps on (self: scala.xml.NodeBuffer) with
  def ≈ (that: scala.xml.NodeBuffer): Boolean =
    val selfIt = self.iterator
    val thatIt = that.iterator
    while selfIt.hasNext && thatIt.hasNext do
      if !(selfIt.next() ≈ thatIt.next()) then
        return false
    selfIt.isEmpty && thatIt.isEmpty

  def !≈ (that: scala.xml.NodeBuffer): Boolean = !(self ≈ that)

