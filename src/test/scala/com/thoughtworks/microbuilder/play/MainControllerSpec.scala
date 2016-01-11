package com.thoughtworks.microbuilder.play

import com.thoughtworks.microbuilder.play.Implicits.scalaFutureToJsonStreamFuture
import jsonStream.rpc.IFuture1
import org.specs2.mutable._
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class MainControllerSpec extends Specification {

  "call myMethod(10, test) " >> {
    "should get the myInnerEntity" >> {

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

      val result: Future[play.api.mvc.Result] = mainController.rpc("/my-method/123/name/test").apply(FakeRequest())

      contentAsString(result) must contain( """"code": 123""")
      contentAsString(result) must contain( """"message": "xxxx"""")
    }

  }
}
