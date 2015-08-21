package com.thoughtworks.restRpc.play

import java.util.concurrent.TimeUnit.SECONDS

import com.github.dreamhead.moco._
import com.thoughtworks.restRpc.core.{RouteConfiguration, UriTemplate}
import org.mockito.Mockito
import org.specs2.Specification
import org.specs2.mock.{Mockito => SpecMockito}

import scala.concurrent._
import scala.concurrent.duration._
import scala.language.implicitConversions
import com.thoughtworks.restRpc.play.Implicits.jsonStreamFutureToScalaFuture

class RpcOutgoingTest extends Specification with SpecMockito {
  def is = s2"""

      This is a specification of using rest-rpc-play tools to make http requests

      Should be able to send a get request without parameters     $e1
      ${step("sequence execution")}
      Should be able to send a get request by different resource     $e2
                                                                  """

  def e1 = {
    val theServer: Runner = createMockServer("/my-method/1/name/abc", """
          {
            "myInnerEntity": {
              "code":1,
              "message":"this is a message"
            }
          }""")



    val myRpc: MyRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_restRpc_play_MyRpc(
      new PlayOutgoingJsonService("http://localhost:8090", configuration)
    )

    val scalaResponseFuture: Future[MyResponse] = myRpc.myMethod(1, "abc")


    val response = Await.result(scalaResponseFuture, Duration(5, SECONDS))
    theServer.stop()
    response.myInnerEntity.code === 1
    response.myInnerEntity.message === "this is a message"
  }

  def e2 = {
    val theServer: Runner = createMockServer("/my-method/2/name/def", """
          {
            "myInnerEntity": {
              "code":2,
              "message":"this is a different message"
            }
          }""")

    val myRpc: MyRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_restRpc_play_MyRpc(
      new PlayOutgoingJsonService("http://localhost:8090", configuration)
    )

    val scalaResponseFuture: Future[MyResponse] = myRpc.myMethod(2, "def")


    val response = Await.result(scalaResponseFuture, Duration(5, SECONDS))
    theServer.stop()
    response.myInnerEntity.code === 2
    response.myInnerEntity.message === "this is a different message"
  }

  def createMockServer(uri: String, response: String): Runner = {
    val monitor = Mockito.mock(classOf[MocoMonitor])

    val server: HttpServer = Moco.httpServer(8090, monitor)
    server.get(Moco.by(Moco.uri(uri))).response(
      response)

    val theServer = Runner.runner(server)
    theServer.start()
    theServer
  }

  def configuration: RouteConfiguration = {
    new RouteConfiguration() {
      override def nameToUriTemplate(name: String): UriTemplate = {
        new UriTemplate {
          override def render(context: scala.Any): String =
            fillInParams(context.asInstanceOf[com.qifun.jsonStream.JsonStream], "my-method/%s/name/%s")

          //By JsonStreamExtractor
          def fillInParams(params: com.qifun.jsonStream.JsonStream, urlWithPlaceHolder: String): String = {
            params match {
              case JsonStreamExtractor.Array(elements) => {
                val params = elements.toStream.map {
                  case JsonStreamExtractor.Integer(number) =>
                    number.toString
                  case JsonStreamExtractor.String(json) =>
                    json
                  case _ => ""
                }
                urlWithPlaceHolder.format(params: _*)
              }
              case _ => ""
            }

          }

          override def method(): String = "GET"
        }
      }
    }
  }

}
