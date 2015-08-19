package com.thoughtworks.restRpc.play

import com.qifun.jsonStream.rpc.IJsonService
import com.thoughtworks.restRpc.core.{IUriTemplate, RouteConfiguration}

class PlayOutgoingJsonService(s: String, routes: RouteConfiguration) extends IJsonService {

  def apply(request: com.qifun.jsonStream.JsonStream, responseHandler: com.qifun.jsonStream.rpc.IJsonResponseHandler): Unit = {
    request match {
      case JsonStreamExtractor.Object(pairs) =>
        val pair = pairs.next()
        pair.value match {
          case JsonStreamExtractor.Array(parameters) => {
            while(parameters.hasNext) {
              val template:IUriTemplate  = routes.nameToUriTemplate.get(pair.key).asInstanceOf[IUriTemplate]
              val method: String = template.get_method()
              val url = template.render(parameters.haxeIterator)
//              val wsapi:WSAPI = ???
              //
              //    val url = getUrl(routes, request)
              //    val body = getBody(request)
              //    val response:ByteBuffer = wsapi.url(url, body)
              //    responseHandler(response.toJsonStream)
            }
          }
        }
      case _ =>
        throw new IllegalArgumentException("parameter should be an array")
    }
  }

  def push(x$1: com.qifun.jsonStream.JsonStream): Unit = {
//    val wsapi:WSAPI = ???
//
//    val url = getUrl(routes, request)
//    val body = getBody(request)
//    wsapi.url(url, body)
  }

}
