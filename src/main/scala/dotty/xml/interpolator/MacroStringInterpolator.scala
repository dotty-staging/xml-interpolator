package dotty.xml.interpolator

import scala.quoted._
import scala.tasty.Reflection
import scala.language.implicitConversions
import scala.quoted.Exprs.LiftedExpr
import scala.quoted.Toolbox.Default._

abstract class MacroStringInterpolator[T] {

  final def apply(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(implicit reflect: Reflection): Expr[T] = {
    try interpolate(strCtxExpr, argsExpr)
    catch {
      case ex: NotStaticlyKnownError =>
        // TODO use ex.expr to recover the position
        throw new QuoteError(ex.getMessage)
      case ex: StringContextError =>
        // TODO use ex.idx to recover the position
        throw new QuoteError(ex.getMessage)
      case ex: ArgumentError =>
        // TODO use ex.idx to recover the position
        throw new QuoteError(ex.getMessage)
    }
  }

  protected def interpolate(strCtxExpr: Expr[StringContext], argsExpr: Expr[Seq[Any]])(implicit reflect: Reflection): Expr[T] =
    interpolate(getStaticStringContext(strCtxExpr), getArgsList(argsExpr))

  protected def interpolate(strCtx: StringContext, argExprs: List[Expr[Any]])(implicit reflect: Reflection): Expr[T]

  protected def getStaticStringContext(strCtxExpr: Expr[StringContext])(implicit reflect: Reflection): StringContext = {
    import reflect._
    strCtxExpr.unseal.underlyingArgument match {
      case Term.Select(Term.Typed(Term.Apply(_, List(Term.Apply(_, List(Term.Typed(Term.Repeated(strCtxArgTrees, _), TypeTree.Inferred()))))), _), _) =>
        val strCtxArgs = strCtxArgTrees.map {
          case Term.Literal(Constant.String(str)) => str
          case tree => throw new NotStaticlyKnownError("Expected statically known StringContext", tree.seal[Any])
        }
        StringContext(strCtxArgs: _*)
      case tree =>
        throw new NotStaticlyKnownError("Expected statically known StringContext", tree.seal[Any])
    }
  }

  protected def getArgsList(argsExpr: Expr[Seq[Any]])(implicit reflect: Reflection): List[Expr[Any]] = {
    import reflect._
    argsExpr.unseal.underlyingArgument match {
      case Term.Typed(Term.Repeated(args, _), _) => args.map(_.seal[Any])
      case tree => throw new NotStaticlyKnownError("Expected statically known argument list", tree.seal[Any])
    }
  }

  protected implicit def StringContextIsLiftable: Liftable[StringContext] = new Liftable[StringContext] {
    def toExpr(strCtx: StringContext): Expr[StringContext] = {
      implicit def ListIsLiftable: Liftable[List[String]] = new Liftable[List[String]] {
        override def toExpr(list: List[String]): Expr[List[String]] = list match {
          case x :: xs => '{~{x.toExpr} :: ~{toExpr(xs)}}
          case Nil => '{Nil}
        }
      }
      '{StringContext(~{strCtx.parts.toList.toExpr}: _*)}
    }
  }

  protected class NotStaticlyKnownError(msg: String, expr: Expr[Any]) extends Exception(msg)
  protected class StringContextError(msg: String, idx: Int, start: Int = -1, end: Int = -1) extends Exception(msg)
  protected class ArgumentError(msg: String, idx: Int) extends Exception(msg)

}