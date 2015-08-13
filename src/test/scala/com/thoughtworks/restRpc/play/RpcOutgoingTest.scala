package com.thoughtworks.restRpc.play

import com.github.dreamhead.moco._
import com.qifun.jsonStream.rpc.IFuture1
import org.mockito.Mockito
import org.specs2.Specification
import org.specs2.mock.{Mockito => SpecMockito}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.implicitConversions
import java.util.concurrent.TimeUnit.SECONDS

object Implicits {
  implicit def jsonStreamFutureToScalaFuture[Value](jsonStreamFuture: IFuture1[Value]):Future[Value] = ???
}

import com.thoughtworks.restRpc.play.Implicits._

class RpcOutgoingTest extends Specification with SpecMockito {def is = s2"""

      This is a specification of using rest-rpc-play tools to make http requests

      Should be able to send a get request without parameters     $e1
                                                                  """

  def e1 = {
    val monitor = Mockito.mock(classOf[MocoMonitor])

    val server: HttpServer = Moco.httpServer(9000, monitor)
    server.get(Moco.by(Moco.uri("/my-method/1/name/abc"))).response(
      """
        {
          "myInnerEntity": {
            "code":1,
            "message":"this is a message"
          }
        }
      """)

    val theServer = Runner.runner(server)
    theServer.start()

    val myRpc:MyRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_restRpc_play_MyRpc(
      new PlayOutgoingJsonService("http://localhost:8080/", MyUriTemplateProcessor.processor_com_thoughtworks_restRpc_play_MyRpc)
    )

    val scalaResponseFuture:Future[MyResponse] = myRpc.myMethod(1, "abc")
    theServer.stop()

    val response = Await.result(scalaResponseFuture, Duration(100, SECONDS))
    response.myInnerEntity.code === 1
    response.myInnerEntity.message === "this is a message"
  }
}
