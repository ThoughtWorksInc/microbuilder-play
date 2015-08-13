package com.thoughtworks.restRpc.play

import com.qifun.jsonStream.rpc.IJsonService
import com.thoughtworks.restRpc.core.RouteConfiguration

class PlayOutgoingJsonService(s: String, routes: RouteConfiguration) extends IJsonService {

  def apply(request: com.qifun.jsonStream.JsonStream, responseHandler: com.qifun.jsonStream.rpc.IJsonResponseHandler): Unit = {
//    val wsapi:WSAPI = ???
//
//    val url = getUrl(routes, request)
//    val body = getBody(request)
//    val response:ByteBuffer = wsapi.url(url, body)
//    responseHandler(response.toJsonStream)
  }

  def push(x$1: com.qifun.jsonStream.JsonStream): Unit = {
//    val wsapi:WSAPI = ???
//
//    val url = getUrl(routes, request)
//    val body = getBody(request)
//    wsapi.url(url, body)
  }

}
