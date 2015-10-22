package com.thoughtworks.microbuilder.play

import com.qifun.jsonStream.JsonStream
import com.qifun.jsonStream.io.TextParser
import com.qifun.jsonStream.rpc.IFuture1
import com.thoughtworks.microbuilder.core.{IRouteConfiguration, IUriTemplate}
import com.thoughtworks.microbuilder.play.Implicits.scalaFutureToJsonStreamFuture
import haxe.root
import org.specs2.mutable._
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

  override def get_failureClassName(): String = ???

  override def matchUri(method: String, uri: String, body: JsonStream, contentType: String): JsonStream = {
    TextParser.parseString( """{"myMethod" : [10, "test"]}""")
  }
}

class MainControllerSpec extends Specification {


  "call myMethod(10, test) " >> {
    "should get the myInnerEntity" >> {

      lazy val routeConfiguration = new RouteConfiguration()

      val rpcEntry = new RpcEntry(routeConfiguration, MyIncomingProxyFactory.incomingProxy_com_thoughtworks_microbuilder_play_MyRpc(new MyRpc {
        override def myMethod(id: Int, name: String): IFuture1[MyResponse] = {
          val response = new MyResponse
          response.myInnerEntity = new MyInnerEntity
          response.myInnerEntity.code = 123
          response.myInnerEntity.message = "xxxx"
          Future.successful(response)
        }

        override def createResource(resourceName: String, body: Book): IFuture1[CreatedResponse] = {
          val createdResponse = new CreatedResponse
          createdResponse.result = """{"result": "created"}"""

          Future.successful(createdResponse)
        }
      }))

      val rpcEntrySeq = Seq(rpcEntry)

      val mainController = new MainController(rpcEntrySeq)

      val result: Future[play.api.mvc.Result] = mainController.rpc("/my-method/test/name/test").apply(FakeRequest())

      contentAsString(result) must contain( """"code": 123""")
      contentAsString(result) must contain( """"message": "xxxx"""")
    }

  }
}
