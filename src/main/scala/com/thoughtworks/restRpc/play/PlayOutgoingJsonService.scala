package com.thoughtworks.restRpc.play

import com.qifun.jsonStream.JsonStream
import com.qifun.jsonStream.io.TextParser
import com.qifun.jsonStream.rpc.IJsonService
import com.thoughtworks.restRpc.core.{IUriTemplate, RouteConfiguration}
import play.api.libs.ws.WSAPI

import scala.concurrent.ExecutionContext
import scala.util.{Success, Failure}

class PlayOutgoingJsonService(urlPrefix: String, routes: RouteConfiguration, wsAPI: WSAPI)(implicit executionContext: ExecutionContext) extends IJsonService {

  def apply(request: com.qifun.jsonStream.JsonStream, responseHandler: com.qifun.jsonStream.rpc.IJsonResponseHandler): Unit = {
    request match {
      case JsonStreamExtractor.Object(pairs) =>
        val pair = pairs.next()
        pair.value match {
          case JsonStreamExtractor.Array(parameters) =>
            val template: IUriTemplate = routes.nameToUriTemplate.get(pair.key).asInstanceOf[IUriTemplate]
            val url = template.render(parameters.haxeIterator)

            wsAPI.url(urlPrefix + url).get().onComplete {
              case Success(response) =>
                responseHandler.onSuccess(TextParser.parseString(response.body))
              case Failure(e) =>
                responseHandler.onFailure(JsonStream.STRING(e.getMessage))
            }
        }
      case _ =>
        throw new IllegalArgumentException("parameter should be an array")
    }
  }

  def push(x$1: com.qifun.jsonStream.JsonStream): Unit = {
  }

}
