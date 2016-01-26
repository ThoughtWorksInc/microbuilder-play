package com.thoughtworks.microbuilder.play

import scala.reflect.macros.Context

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
private[play] object Macros {

  private def methodName(c: Context)(prefix: String, symbol: c.universe.Symbol): String = {
    import c.universe._
    val methodNameBuilder = new StringBuilder
    methodNameBuilder ++= prefix
    def buildMethodName(symbol: Symbol) {
      val owner = symbol.owner
      if (owner != NoSymbol) {
        buildMethodName(owner)
        methodNameBuilder += '_'
        methodNameBuilder ++= symbol.name.toString
      }
    }
    buildMethodName(symbol)
    methodNameBuilder.toString
  }

  def newIncomingStub(c: Context): c.Expr[Nothing] = {
    import c.universe._

    val TypeApply(_, List(serviceTypeTree)) = c.macroApplication


    c.Expr(
      q"""
        new _root_.com.thoughtworks.microbuilder.play.IncomingStub[$serviceTypeTree] {
          override def routeConfiguration: _root_.com.thoughtworks.microbuilder.core.IRouteConfiguration = {
            ${Ident(newTermName(methodName(c)("routeConfiguration", serviceTypeTree.tpe.typeSymbol)))}
          }
          override def incomingServiceProxy(serviceImplementation: $serviceTypeTree): _root_.jsonStream.rpc.IJsonService = {
            ${Ident(newTermName(methodName(c)("incomingProxy", serviceTypeTree.tpe.typeSymbol)))}(serviceImplementation)
          }
        }
      """)
  }

  def newOutgoingStub(c: Context): c.Expr[Nothing] = {
    import c.universe._

    val TypeApply(_, List(serviceTypeTree)) = c.macroApplication

    c.Expr(
      q"""
        new _root_.com.thoughtworks.microbuilder.play.OutgoingStub[$serviceTypeTree] {
          override def routeConfiguration: _root_.com.thoughtworks.microbuilder.core.IRouteConfiguration = {
            ${Ident(newTermName(methodName(c)("routeConfiguration", serviceTypeTree.tpe.typeSymbol)))}
          }
          override def outgoingServiceProxy(jsonService: _root_.jsonStream.rpc.IJsonService): $serviceTypeTree = {
            ${Ident(newTermName(methodName(c)("outgoingProxy", serviceTypeTree.tpe.typeSymbol)))}(jsonService)
          }
        }
      """)
  }
}
