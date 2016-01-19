package com.thoughtworks.microbuilder.play

import com.thoughtworks.microbuilder.play.Implicits.scalaFutureToJsonStreamFuture
import com.thoughtworks.microbuilder.play.exception.MicrobuilderException.{WrongResponseFormatException, NativeException, StructuralApplicationException, TextApplicationException}
import jsonStream.rpc.IFuture1
import org.specs2.mutable._
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.test.FakeRequest
import play.api.test.Helpers._
import scala.concurrent.duration._

import scala.concurrent.{Await, Future}


class RpcControllerExceptionSpec extends Specification {

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

      val mainController = new RpcController(rpcEntrySeq)

      val result: Future[play.api.mvc.Result] = mainController.rpc("/my-method/test/name/test").apply(FakeRequest())

      status(result) must equalTo(BAD_REQUEST)
    }
  }

  "RpcController" >> {

    "should send text response when RPC implementation throws a TextApplicationException" >> {
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

      val mainController = new RpcController(rpcEntrySeq)

      val result: Future[play.api.mvc.Result] = mainController.rpc("/my-method/12345/name/test").apply(FakeRequest())

      contentAsString(result) must equalTo("app exception")
      status(result) must equalTo(INSUFFICIENT_STORAGE)
    }

    "should send JSON response when RPC implementation throws a StructuralApplicationException" >> {
      lazy val routeConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_MyRpcWithStructuralException()

      val rpcEntry = new RpcEntry(routeConfiguration, MyIncomingProxyFactory.incomingProxy_com_thoughtworks_microbuilder_play_MyRpcWithStructuralException(new MyRpcWithStructuralException {
        override def myMethod(id: Int, name: String): IFuture1[MyResponse] = {
          val failure = new GeneralFailure()
          failure.errorMsg = "my error message"
          Future.failed(new StructuralApplicationException(failure, INSUFFICIENT_STORAGE))
        }
      }))

      val rpcEntrySeq = Seq(rpcEntry)

      val mainController = new RpcController(rpcEntrySeq)

      val result: Future[play.api.mvc.Result] = mainController.rpc("/my-method/12345/name/test").apply(FakeRequest())

      contentAsJson(result) must equalTo(JsObject(Map("errorMsg" -> JsString("my error message"))))
      status(result) must equalTo(INSUFFICIENT_STORAGE)
    }

    "should throw a NativeException when RPC implementation throws other exceptions" >> {
      lazy val routeConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_MyRpcWithStructuralException()

      val rpcEntry = new RpcEntry(routeConfiguration, MyIncomingProxyFactory.incomingProxy_com_thoughtworks_microbuilder_play_MyRpcWithStructuralException(new MyRpcWithStructuralException {
        override def myMethod(id: Int, name: String): IFuture1[MyResponse] = {
          Future.failed(new Exception("my exception message"))
        }
      }))

      val rpcEntrySeq = Seq(rpcEntry)

      val mainController = new RpcController(rpcEntrySeq)

      val result: Future[play.api.mvc.Result] = mainController.rpc("/my-method/12345/name/test").apply(FakeRequest())

      Await.result(result, 1.seconds) must throwA[NativeException].like {
        case e =>
          e.getMessage must contain("my exception message")
      }
    }

    "should throw a WrongResponseFormatException when RPC implementation throws a TextApplicationException if failureClassName is not omitted" >> {
      lazy val routeConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_MyRpcWithStructuralException()

      val rpcEntry = new RpcEntry(routeConfiguration, MyIncomingProxyFactory.incomingProxy_com_thoughtworks_microbuilder_play_MyRpcWithStructuralException(new MyRpcWithStructuralException {
        override def myMethod(id: Int, name: String): IFuture1[MyResponse] = {
          Future.failed(new TextApplicationException("app exception", INSUFFICIENT_STORAGE))
        }
      }))

      val rpcEntrySeq = Seq(rpcEntry)

      val mainController = new RpcController(rpcEntrySeq)

      val result: Future[play.api.mvc.Result] = mainController.rpc("/my-method/12345/name/test").apply(FakeRequest())

      Await.result(result, 1.seconds) must throwA[WrongResponseFormatException]
    }

    "should throw a WrongResponseFormatException when RPC implementation throws a StructuralApplicationException if failureClassName is omitted" >> {
      lazy val routeConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_MyRpc()

      val rpcEntry = new RpcEntry(routeConfiguration, MyIncomingProxyFactory.incomingProxy_com_thoughtworks_microbuilder_play_MyRpc(new MyRpc {

        override def myMethod(id: Int, name: String): IFuture1[MyResponse] = {
          val failure = new GeneralFailure()
          failure.errorMsg = "my error message"
          Future.failed(new StructuralApplicationException(failure, INSUFFICIENT_STORAGE))
        }

        override def createResource(resourceName: String, body: Book): IFuture1[CreatedResponse] = {
          val createdResponse = new CreatedResponse
          createdResponse.result = """{"result": "created"}"""
          Future.successful(createdResponse)
        }
      }))

      val rpcEntrySeq = Seq(rpcEntry)

      val mainController = new RpcController(rpcEntrySeq)

      val result: Future[play.api.mvc.Result] = mainController.rpc("/my-method/12345/name/test").apply(FakeRequest())

      Await.result(result, 1.seconds) must throwA[WrongResponseFormatException]
    }


  }
}
