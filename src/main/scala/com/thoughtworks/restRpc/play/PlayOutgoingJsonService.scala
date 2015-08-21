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
    clientRunning {
      client =>
        client.url(generateURL(request)).get() map {
          res => responseHandler.onSuccess(TextParser.parseString(res.json.toString()))
        }

    }
  }
    //By JsonStreamExtractor
  def generateURL(request: com.qifun.jsonStream.JsonStream): String = {
    val metaData = Map("myMethod" -> "my-method")

    request match {
      case JsonStreamExtractor.Object(pairs) => {
        pairs.map {
          a =>
            s"${host}/${metaData.getOrElse(a.key, "error")}" + fillInParams(a.value, "/%s/name/%s")
        }.mkString
      }
      case _ => ""
    }
  }

  //By Json.parse
  def generateURL2(request: com.qifun.jsonStream.JsonStream): String = {
    val metaData = Map("myMethod" -> "my-method")

    val requestJsonData: JsObject = Json.parse(PrettyTextPrinter.toString(request)).as[JsObject]
    val methodName: Set[String] = requestJsonData.keys
    val params: JsArray = requestJsonData.value.get(methodName.head).get.as[JsArray]
    s"${host}/${metaData.getOrElse(methodName.head, "error")}" + "/%s/name/%s".format(params.value : _*).replaceAll("\"", "")
  }

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
