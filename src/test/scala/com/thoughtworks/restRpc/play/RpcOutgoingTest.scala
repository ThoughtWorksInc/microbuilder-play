package com.thoughtworks.restRpc.play

import java.util.concurrent.TimeUnit.SECONDS

import com.qifun.jsonStream.rpc.{ICompleteHandler1, IFuture1}
import com.thoughtworks.restRpc.core.{IUriTemplate, IRouteConfiguration}
import mockws.MockWS
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import play.api.libs.ws.WSAPI
import play.api.mvc.Action
import play.api.mvc.Results._
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
    case (POST, "http://localhost:8080/books") => Action {
      Created("created")
    }
  }

  val mockWsApi = new WSAPI {
    override def url(url: String) = ws.url(url)
    override def client = ws
  }

  "This is a specification of using rest-rpc-play tools to make http requests".txt

  "Should convert myMethod to http get request and get the response" >> {
    val configuration: IRouteConfiguration = mock[IRouteConfiguration]
    val template: IUriTemplate = new FakeUriTemplate("GET", "/my-method/1.0/name/abc", 2)
    configuration.nameToUriTemplate("myMethod") returns template

    val myRpc: MyRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_restRpc_play_MyRpc(
      new PlayOutgoingJsonService("http://localhost:8080", configuration, mockWsApi)
    )

    val response = Await.result(myRpc.myMethod(1, "abc"), Duration(5, SECONDS))

    response.myInnerEntity.message === "this is a message"
    response.myInnerEntity.code === 1
  }

  "Should convert createResource to http post request and get created response" >> {
    val configuration: IRouteConfiguration = mock[IRouteConfiguration]

    val template: IUriTemplate = new FakeUriTemplate("POST", "/books", 1)
    configuration.nameToUriTemplate("createResource") returns template

    val myRpc: MyRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_restRpc_play_MyRpc(
      new PlayOutgoingJsonService("http://localhost:8080", configuration, mockWsApi)
    )

    val response = Await.result(myRpc.createResource("books", new Book(1, "name")), Duration(5, SECONDS))

    response === "created"
  }

}
