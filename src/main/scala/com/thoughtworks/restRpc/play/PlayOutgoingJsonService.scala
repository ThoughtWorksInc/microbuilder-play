package com.thoughtworks.restRpc.play

import com.qifun.jsonStream.JsonStream
import com.qifun.jsonStream.io.{PrettyTextPrinter, TextParser}
import com.qifun.jsonStream.rpc.{ICompleteHandler1, IFuture1, IJsonService}
import com.thoughtworks.restRpc.core.RouteConfiguration
import play.api.Logger
import play.api.libs.json.{JsArray, JsObject, Json}

import scala.collection.Set
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._


object Implicits {
  implicit def jsonStreamFutureToScalaFuture[Value](jsonStreamFuture: IFuture1[Value]): Future[Value] = {
    val p = promise[Value]

    jsonStreamFuture.start(new ICompleteHandler1[Value] {
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

    val requestJsonData: JsObject = Json.parse(PrettyTextPrinter.toString(request)).as[JsObject]

    val methodName: Set[String] = requestJsonData.keys
    val metaData = Map("myMethod" -> "my-method")
    clientRunning {
      client =>
        client.url(generateURL(requestJsonData, methodName, metaData)).get() map {
          res =>
            val raw: JsonStream = TextParser.parseString(res.json.toString())
            responseHandler.onSuccess(raw)
        }
    }
  }

  def generateURL(requestJsonData: JsObject, methodName: Set[String], metaData: Map[String, String]): String = {
    val params: JsArray = requestJsonData.value.get(methodName.head).get.as[JsArray]
    s"${host}/${metaData.getOrElse(methodName.head, "error")}/${params(0)}/name/${params(1).as[String]}"
  }

  def clientRunning(callback: ((play.api.libs.ws.ning.NingWSClient) => Future[Unit])) = {
    val builder = new com.ning.http.client.AsyncHttpClientConfig.Builder()
    val client = new play.api.libs.ws.ning.NingWSClient(builder.build())
    callback(client).onComplete(_ => client.close())

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
