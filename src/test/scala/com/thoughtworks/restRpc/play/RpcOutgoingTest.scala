package com.thoughtworks.restRpc.play

import java.util.concurrent.TimeUnit.SECONDS

import com.github.dreamhead.moco.{Moco, _}
import com.ning.http.client.AsyncHttpClientConfig
import com.qifun.jsonStream.rpc.{ICompleteHandler1, IFuture1}
import com.thoughtworks.restRpc.core.{IRouteConfiguration, IUriTemplate}
import org.specs2.mock.{Mockito => SpecMockito}
import org.specs2.mutable.Specification

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import scala.language.implicitConversions
import play.api.libs.ws._
import play.api.libs.ws.ning._

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

class RpcOutgoingTest extends Specification with SpecMockito {
  val ws:WSClient = new NingWSClient(new AsyncHttpClientConfig.Builder().build())

  val mockWsApi = new WSAPI {
    override def url(url: String) = ws.url(url)
    override def client = ws
  }

  "This is a specification of using rest-rpc-play tools to make http requests".txt

  "Should convert myMethod to http get request and get the response" >> {
    val server: HttpServer = Moco.httpServer(8090)
    server.get(Moco.by(Moco.uri("/my-method/1.0/name/abc"))).response("""
          {
            "myInnerEntity": {
              "code":1,
              "message":"this is a message"
            }
          }""")

    val theServer = Runner.runner(server)
    theServer.start()

    val configuration: IRouteConfiguration = mock[IRouteConfiguration]
    val template: IUriTemplate = new FakeUriTemplate("GET", "/my-method/1.0/name/abc", 2)
    configuration.nameToUriTemplate("myMethod") returns template

    val myRpc: MyRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_restRpc_play_MyRpc(
      new PlayOutgoingJsonService("http://localhost:8090", configuration, mockWsApi)
    )

    val response = Await.result(myRpc.myMethod(1, "abc"), Duration(5, SECONDS))

    theServer.stop()
    ws.close()

    response.myInnerEntity.message === "this is a message"
    response.myInnerEntity.code === 1
  }

//  "Should convert createResource to http post request and get created response" >> {
//    val server: HttpServer = Moco.httpServer(8080)
//    server.post(Moco.by(Moco.uri("/books"))).response("created")
//
//    val theServer = Runner.runner(server)
//    theServer.start()
//
//    val configuration: IRouteConfiguration = mock[IRouteConfiguration]
//
//    val template: IUriTemplate = new FakeUriTemplate("POST", "/books", 1)
//    configuration.nameToUriTemplate("createResource") returns template
//
//    val myRpc: MyRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_restRpc_play_MyRpc(
//      new PlayOutgoingJsonService("http://localhost:8080", configuration, mockWsApi)
//    )
//
//    val response = Await.result(myRpc.createResource("books", new Book(1, "name")), Duration(5, SECONDS))
//
//    theServer.stop()
//    ws.close()
//
//    response === "created"
//  }
}
