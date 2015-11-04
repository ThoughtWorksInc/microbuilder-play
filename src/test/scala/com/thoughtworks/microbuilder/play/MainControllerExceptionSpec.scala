package com.thoughtworks.microbuilder.play

import com.thoughtworks.microbuilder.play.Implicits.scalaFutureToJsonStreamFuture
import com.thoughtworks.microbuilder.play.exception.MicrobuilderException.TextApplicationException
import jsonStream.rpc.IFuture1
import org.specs2.mutable._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future


class MainControllerExceptionSpec extends Specification {

  "the parameter type didn't matched" >> {
    "should return an error" >> {
      lazy val routeConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_MyRpc()

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

      status(result) must equalTo(BAD_REQUEST)
//      contentAsString(result) must contain( """"error": "type didn't matched"""")
    }
  }


  "app error" >> {
    "should return an app error" >> {
      lazy val routeConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_MyRpc()

      val rpcEntry = new RpcEntry(routeConfiguration, MyIncomingProxyFactory.incomingProxy_com_thoughtworks_microbuilder_play_MyRpc(new MyRpc {
        override def myMethod(id: Int, name: String): IFuture1[MyResponse] = {
          Future.failed(new TextApplicationException("app exception", INSUFFICIENT_STORAGE))
        }

        override def createResource(resourceName: String, body: Book): IFuture1[CreatedResponse] = {
          val createdResponse = new CreatedResponse
          createdResponse.result = """{"result": "created"}"""

          Future.successful(createdResponse)
        }
      }))

      val rpcEntrySeq = Seq(rpcEntry)

      val mainController = new MainController(rpcEntrySeq)

      val result: Future[play.api.mvc.Result] = mainController.rpc("/my-method/12345/name/test").apply(FakeRequest())

      contentAsString(result) must equalTo("app exception")
      status(result) must equalTo(INSUFFICIENT_STORAGE)
      //      contentAsString(result) must contain( """"error": "type didn't matched"""")
    }
  }
}
