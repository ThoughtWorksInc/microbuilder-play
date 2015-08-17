package com.thoughtworks.restRpc.play

import com.qifun.jsonStream.JsonStream
import com.qifun.jsonStream.io.TextParser
import com.qifun.jsonStream.rpc.{ICompleteHandler1, IFuture1, IJsonService}
import com.thoughtworks.restRpc.core.RouteConfiguration
import play.api.Logger

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._


object Implicits {
  implicit def jsonStreamFutureToScalaFuture[Value](jsonStreamFuture: IFuture1[Value]):Future[Value] = {
    val p = promise[Value]

    jsonStreamFuture.start(new ICompleteHandler1[Value]{
      override def onFailure(o: scala.Any): Unit =
        p failure new Exception

      override def onSuccess(awaitResult: Value): Unit =
        p success awaitResult
    })
    p.future
  }
}

class PlayOutgoingJsonService(host: String, routes: RouteConfiguration) extends IJsonService {

  def apply(request: com.qifun.jsonStream.JsonStream, responseHandler: com.qifun.jsonStream.rpc.IJsonResponseHandler): Unit = {
    // Standard Play-style WSAPI
    val client = {
      val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder()
      new play.api.libs.ws.ning.NingWSClient(builder.build())
    }
//    val wsAPI: WSAPI = new StandaloneWSAPI()
    val url: String = s"${host}/my-method/1/name/abc"
    client.url(url).get() map {
      res =>
        val raw: JsonStream = TextParser.parseString(res.json.toString())
        responseHandler.onSuccess(raw)
        client.close()
    }
  }

  def push(x$1: com.qifun.jsonStream.JsonStream): Unit = {
    //    val wsapi:WSAPI = ???
    //
    //    val url = getUrl(routes, request)
    //    val body = getBody(request)
    //    wsapi.url(url, body)
    Logger.info("Standard Play-style WSAPI push")
  }

}
