package com.thoughtworks.microbuilder.play

import java.util.concurrent.TimeUnit.SECONDS

import com.github.dreamhead.moco.{Moco, _}
import com.ning.http.client.AsyncHttpClientConfig
import com.thoughtworks.microbuilder.core.IRouteConfiguration
import com.thoughtworks.microbuilder.play.Implicits._
import com.thoughtworks.microbuilder.play.exception.MicrobuilderException._
import org.specs2.mock.{Mockito => SpecMockito}
import org.specs2.mutable.Specification
import org.specs2.specification.{AfterAll, BeforeAll}
import play.api.libs.ws._
import play.api.libs.ws.ning._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.language.implicitConversions


class RpcOutgoingWithFailureTest extends Specification with SpecMockito with BeforeAll with AfterAll {
  val ws: WSClient = new NingWSClient(new AsyncHttpClientConfig.Builder().build())

  val mockWsApi = new WSAPI {
    override def url(url: String) = ws.url(url)

    override def client = ws
  }

  var theServer: Runner = null

  "This is a specification of how rest rpc handle server error response".txt

  val configuration: IRouteConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_MyRpcWithStructuralException

  val myRpc: MyRpcWithStructuralException = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_microbuilder_play_MyRpcWithStructuralException(
    new PlayOutgoingJsonService("http://localhost:8091", configuration, mockWsApi)
  )

  "Should throw StructuralApplicationException with STRUCTURAL_APPLICATION_FAILURE when structuralFailure is configured" >> {
    Await.result(myRpc.myMethod(1, "failure"), Duration(5, SECONDS)) must (throwA like {
      case StructuralApplicationException(generalFailure, code) =>
        generalFailure.asInstanceOf[GeneralFailure].errorMsg must equalTo("not found")
        code must equalTo(404)
    })
  }

  "Should throw WrongResponseFormatException if json is not correct " >> {
    Await.result(myRpc.myMethod(1, "wrong-json"), Duration(5, SECONDS)) must (throwA like {
      case WrongResponseFormatException(errorMsg) =>
        errorMsg must equalTo("Wrong Json format: not a json")
    })
  }

  def beforeAll() {
    val server = Moco.httpServer(8091)
    server.get(Moco.by(Moco.uri("/my-method/1/name/failure"))).response(Moco.`with`(Moco.text("{\"errorMsg\":\"not found\"}")), Moco.status(404))
    server.get(Moco.by(Moco.uri("/my-method/1/name/wrong-json"))).response(Moco.`with`(Moco.text("not a json")), Moco.status(200))

    theServer = Runner.runner(server)
    theServer.start()
  }

  override def afterAll() = {
    theServer.stop()
    ws.close()
  }
}
