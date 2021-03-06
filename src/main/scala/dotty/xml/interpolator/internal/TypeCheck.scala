package dotty.xml.interpolator
package internal

import scala.quoted._
import scala.tasty._

import dotty.xml.interpolator.internal.Tree._

object TypeCheck {
  def apply(nodes: Seq[Node]) given XmlContext, Reporter, Reflection: Seq[Node] = {
    typecheck(nodes)
    nodes
  }

  private def typecheck(nodes: Seq[Node]) given XmlContext, Reporter given (reflect: Reflection): Unit = {
    import reflect._
    nodes.foreach {
      case elem : Elem =>
        elem.attributes.foreach(attribute =>
          attribute.value match {
            case Seq(Placeholder(id)) =>
              val dummy = '{ _root_.scala.xml.TopScope }
              val expr = the[XmlContext].args(id)
              val term = expr.apply(dummy).unseal
              val expected = attribute.isNamespace match {
                case true => Seq('[String].unseal.tpe)
                case _ => Seq(
                  '[String].unseal.tpe,
                  '[Seq[scala.xml.Node]].unseal.tpe,
                  '[Option[Seq[scala.xml.Node]]].unseal.tpe
                )
              }
              if (!expected.exists(term.tpe <:< _)) {
                the[Reporter].error(
                  s"""type mismatch;
                    | found   : ${term.tpe.widen.show}
                    | required: ${expected.map(_.show).mkString(" | ")}
                  """.stripMargin, term.seal)
              }
            case _ =>
        })
        typecheck(elem.children)
      case _ =>
    }
  }
}