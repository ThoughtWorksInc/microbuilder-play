package com.thoughtworks.microbuilder.play

import java.util.concurrent.TimeUnit.SECONDS

import com.github.dreamhead.moco.{Moco, _}
import com.ning.http.client.AsyncHttpClientConfig
import com.thoughtworks.microbuilder.core.{IRouteConfiguration, IRouteEntry}
import com.thoughtworks.microbuilder.play.Implicits._
import com.thoughtworks.microbuilder.play.exception.MicrobuilderException.{TextApplicationException, NativeException, WrongResponseFormatException}
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
class RpcOutgoingTest extends Specification with SpecMockito with BeforeAll with AfterAll {
  val ws:WSClient = new NingWSClient(new AsyncHttpClientConfig.Builder().build())

  val mockWsApi = new WSAPI {
    override def url(url: String) = ws.url(url)
    override def client = ws
  }

  var theServer: Runner = null

  val configuration: IRouteConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_MyRpc

  val myRpc: MyRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_microbuilder_play_MyRpc(
    new PlayOutgoingJsonService("http://localhost:8090", configuration, mockWsApi)
  )

  "This is a specification of using microbuilder-play tools to make http requests".txt

  "Should throw TextApplicationException with TEXT_APPLICATION_FAILURE when structuralFailure is not configured" >> {
    Await.result(myRpc.myMethod(1, "failure"), Duration(5, SECONDS)) must throwA.like {
      case TextApplicationException(textError, code) =>{
        textError === "server error"
        code === 500
      }
    }
  }

  "Should convert myMethod to http get request and get the response" >> {
    val response = Await.result(myRpc.myMethod(1, "abc"), Duration(5, SECONDS))

    response.myInnerEntity.message === "this is a message"
    response.myInnerEntity.code === 1
  }

  "Should convert createResource to http post request and get created response" >> {
    val response = Await.result(myRpc.createResource("books", new Book(1, "name")), Duration(5, SECONDS))

    response.result === "created"
  }

  "Should throw native exception if the response is not legal json" >> {
    Await.result(myRpc.myMethod(1, "wrong_json"), Duration(5, SECONDS)) must throwA.like {
      case WrongResponseFormatException(textError) => textError === "Wrong Json format: not a json"
    }
  }

  def beforeAll() {
    val server = Moco.httpServer(8090)
    server.get(Moco.by(Moco.uri("/my-method/1/name/abc"))).response("""
          {
            "myInnerEntity": {
              "code":1,
              "message":"this is a message"
            }
          }""")

    server.get(Moco.by(Moco.uri("/my-method/1/name/failure"))).response(Moco.`with`(Moco.text("server error")), Moco.status(500))
    server.get(Moco.by(Moco.uri("/my-method/1/name/wrong_json"))).response(Moco.`with`(Moco.text("not a json")), Moco.status(200))

    server.post(Moco.by(Moco.uri("/books"))).response(
      """
        {"result":"created"}
      """)

    theServer = Runner.runner(server)
    theServer.start()
  }

  override def afterAll()  = {
    theServer.stop()
    ws.close()
  }
}
