package com.thoughtworks.restRpc.play

import java.util.concurrent.TimeUnit.SECONDS

import com.qifun.jsonStream.rpc.{ICompleteHandler1, IFuture1}
import com.thoughtworks.restRpc.core.IRouteConfiguration
import mockws.MockWS
import org.specs2.Specification
import org.specs2.mock.Mockito
import play.api.libs.ws.WSAPI
import play.api.mvc.Action
import play.api.mvc.Results.Ok
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

object Implicits {

  implicit def jsonStreamFutureToScalaFuture[Value](jsonStreamFuture: IFuture1[Value]):Future[Value] = {
    val p = Promise[Value]()

    jsonStreamFuture.start(new ICompleteHandler1[Value] {
      override def onSuccess(value: Value): Unit = p success value

      override def onFailure(ex: scala.Any): Unit = p failure ex.asInstanceOf[Throwable]
    })

    p.future
  }
}

import com.thoughtworks.restRpc.play.Implicits._

class RpcOutgoingTest extends Specification with Mockito {
  def is = s2"""

      This is a specification of using rest-rpc-play tools to make http requests

      code should equals to 1                      $e1
      message should equals to "this is a message" $e2
                                                                  """

  val ws: MockWS = MockWS {
    case (GET, "http://localhost:8080/my-method/1.0/name/abc") => Action {
      Ok("""
        {
          "myInnerEntity": {
            "code":1,
            "message":"this is a message"
          }
        }
      """)
    }
  }

  val mockWsApi = new WSAPI {
    override def url(url: String) = ws.url(url)
    override def client = ws
  }
  val configuration: IRouteConfiguration = mock[IRouteConfiguration]
  configuration.nameToUriTemplate("myMethod") returns new FakeUriTemplate

  val myRpc: MyRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_restRpc_play_MyRpc(
    new PlayOutgoingJsonService("http://localhost:8080", configuration, mockWsApi)
  )

  val scalaResponseFuture: Future[MyResponse] = myRpc.myMethod(1, "abc")

  val response = Await.result(scalaResponseFuture, Duration(100, SECONDS))

  def e1 = response.myInnerEntity.message === "this is a message"
  def e2 = response.myInnerEntity.code === 1
}
