package com.thoughtworks.restRpc.callee

import com.qifun.jsonStream.JsonStream
import com.qifun.jsonStream.io.TextParser
import com.qifun.jsonStream.rpc.{IJsonResponseHandler, IJsonService}
import com.thoughtworks.restRpc.core.{IRouteConfiguration, IUriTemplate}
import com.thoughtworks.restRpc.play.{MyRpc, MyIncomingProxyFactory}
import haxe.root
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class RouteConfiguration extends IRouteConfiguration {
  override def nameToUriTemplate(name: String): IUriTemplate = throw new Exception("not implemented")

  override def __hx_deleteField(field: String): Boolean = ???

  override def __hx_getField(field: String, throwErrors: Boolean, isCheck: Boolean, handleProperties: Boolean): AnyRef = ???

  override def __hx_lookupSetField(field: String, value: scala.Any): AnyRef = ???

  override def __hx_setField(field: String, value: scala.Any, handleProperties: Boolean): AnyRef = ???

  override def __hx_getField_f(field: String, throwErrors: Boolean, handleProperties: Boolean): Double = ???

  override def __hx_lookupField(field: String, throwErrors: Boolean, isCheck: Boolean): AnyRef = ???

  override def __hx_setField_f(field: String, value: Double, handleProperties: Boolean): Double = ???

  override def __hx_invokeField(field: String, dynargs: root.Array[_]): AnyRef = ???

  override def __hx_getFields(baseArr: root.Array[String]): Unit = ???

  override def __hx_lookupSetField_f(field: String, value: Double): Double = ???

  override def __hx_lookupField_f(field: String, throwErrors: Boolean): Double = ???

  override def matchUri(uri: String): Array[JsonStream] = new Array[JsonStream](2)
}

@RunWith(classOf[JUnitRunner])
class MainControllerSpec extends Specification {


  "call add(1, 2) === 3" in {

    val rpcEntry = new RpcEntry(new RouteConfiguration(), MyIncomingProxyFactory.incomingProxy_com_thoughtworks_restRpc_play_MyRpc())

    val rpcEntrySeq = Seq.empty[RpcEntry]

    rpcEntrySeq :+ rpcEntry

    val mainController = new MainController(rpcEntrySeq)

    val result: Future[play.api.mvc.Result] = mainController.rpc("/method/name/1/2").apply(FakeRequest())

    val expected = TextParser.parseString( """{"result": "3"}""")

    contentAsBytes(result) must equalTo(expected)
  }
}
