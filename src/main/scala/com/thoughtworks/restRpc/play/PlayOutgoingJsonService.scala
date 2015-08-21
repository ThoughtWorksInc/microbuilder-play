package com.thoughtworks.restRpc.play

import com.qifun.jsonStream.{JsonStreamPair, JsonStream}
import com.qifun.jsonStream.io.{PrettyTextPrinter, TextParser}
import com.qifun.jsonStream.rpc.{ICompleteHandler1, IFuture1, IJsonService}
import com.thoughtworks.restRpc.core.{UriTemplate, RouteConfiguration}
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
    clientRunning {
      client =>
        extractRoute(request) map {
          case r =>
            val template: UriTemplate = routes.nameToUriTemplate(r.name)
            val uri: String = template.render(r.parameters)
            client.url(s"$host/$uri").get() map {
              res => responseHandler.onSuccess(TextParser.parseString(res.json.toString()))
            }
        } getOrElse Future{}
    }
  }

  case class RouteInfo(name: String, parameters: JsonStream)

  def extractRoute(request: JsonStream): Option[RouteInfo] = request match {
    case JsonStreamExtractor.Object(pairs) =>
      val next: JsonStreamPair = pairs.next()
      Some(RouteInfo(next.key, next.value))
    case _ => None
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
