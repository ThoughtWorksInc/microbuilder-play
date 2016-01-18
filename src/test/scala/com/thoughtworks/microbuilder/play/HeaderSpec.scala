package com.thoughtworks.microbuilder.play

import java.util.concurrent.TimeUnit.SECONDS

import com.github.dreamhead.moco.{Moco, _}
import com.ning.http.client.AsyncHttpClientConfig
import com.thoughtworks.microbuilder.core.IRouteConfiguration
import com.thoughtworks.microbuilder.play.Implicits._
import com.thoughtworks.microbuilder.play.exception.MicrobuilderException.{TextApplicationException, WrongResponseFormatException}
import org.junit.runner.RunWith
import org.specs2.mock.{Mockito => SpecMockito}
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import org.specs2.specification.{AfterAll, BeforeAll}
import play.api.libs.ws._
import play.api.libs.ws.ning._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.language.implicitConversions

@RunWith(classOf[JUnitRunner])
class HeaderSpec extends Specification with SpecMockito with BeforeAll with AfterAll {
  val ws:WSClient = new NingWSClient(new AsyncHttpClientConfig.Builder().build())

  val mockWsApi = new WSAPI {
    override def url(url: String) = ws.url(url)
    override def client = ws
  }

  var theServer: Runner = null
  "Proxy for ICustomHeader" >> {
    val configuration: IRouteConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_ICustomHeader

    val myRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_microbuilder_play_ICustomHeader(
      new PlayOutgoingJsonService("http://localhost:8092/", configuration, mockWsApi)
    )
      //
      //
      // "Proxy for ICustomHeader" >> {
      //   val configuration: IRouteConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_ICustomHeader
      //
      //   val headerService: ICustomHeader = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_microbuilder_play_ICustomHeader(
      //     new PlayOutgoingJsonService("http://localhost:8092/", configuration, mockWsApi)
      //   )
      //
      //   "when myMethod is invoked" >> {
      //
      //     val response = Await.result(headerService.myMethod(1, "abc", "my-header-value2"), Duration(5, SECONDS))
      //
      //     "should have correct response" >> {
      //       response.myInnerEntity.message === "this is a message"
      //       response.myInnerEntity.code === 1
      //     }
      //   }
      // }
      //
      "when myMethod is invoked" >> {
        val response = Await.result(myRpc.myMethod(1, "abc", "my-header-value2"), Duration(5, SECONDS))

        response.myInnerEntity.message === "this is a message"
        response.myInnerEntity.code === 1
      }
    }
    def beforeAll() {
      val server = Moco.httpServer(8092)
      import Moco._
      // server.get(Moco.by(Moco.uri("/my-method/1/name/abc"))).response("""
     server.get(and(
       by(uri("/my-method/1/name/abc")),
       Moco.eq(header("My-Custom-Static-Header"), "my-value"),
       Moco.eq(header("My-Custom-Dynamic-Header-0"), "1"),
       Moco.eq(header("My-Custom-Dynamic-Header-1"), "abc"),
       Moco.eq(header("My-Custom-Dynamic-Header-2"), "my-header-value2"))
     ).response("""
            {
              "myInnerEntity": {
                "code":1,
                "message":"this is a message"
              }
            }""")

      theServer = Runner.runner(server)
      theServer.start()
    }

    override def afterAll()  = {
      theServer.stop()
      ws.close()
    }
    /*
  override def beforeAll() {
    val server = Moco.httpServer(8093)
    import Moco._
//    server.get(and(by(uri("/my-method/1/name/abc")), Moco.eq(header("My-Custom-Static-Header"), "my-value"))).response("""
    server.get(Moco.by(Moco.uri("/my-method/1/name/abc"))).response("""
          {
            "myInnerEntity": {
              "code":1,
              "message":"this is a message"
            }
          }""")
    theServer = Runner.runner(server)
    theServer.start()
  }

  override def afterAll()  = {
    theServer.stop()
    ws.close()
  }

  val ws:WSClient = new NingWSClient(new AsyncHttpClientConfig.Builder().build())

  val mockWsApi = new WSAPI {
    override def url(url: String) = ws.url(url)
    override def client = ws
  }

  var theServer: Runner = null

*/
}
