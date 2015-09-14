package com.thoughtworks.restRpc.callee

import akka.actor.Status.Success
import com.qifun.jsonStream.JsonStream
import com.qifun.jsonStream.rpc.{IJsonResponseHandler, IJsonService}
import com.thoughtworks.restRpc.core.IRouteConfiguration
import play.api.mvc._

import scala.concurrent.Promise

case class RpcEntry(routeConfiguration: IRouteConfiguration, incomingServiceProxy: IJsonService)


class MainController(rpcImplementations: Seq[RpcEntry]) extends Controller {

  def remoteCall(uri: String, bodyJsonStream: JsonStream): scala.concurrent.Future[Result] = {
    val promise = Promise[Result]

    rpcImplementations.map(rpcImplementation => {
      rpcImplementation.routeConfiguration.matchUri(uri) match {
        case null =>
        case uriParameters: Array[JsonStream] => {
          val allParameters = uriParameters :+ bodyJsonStream
          val resp: IJsonResponseHandler = new IJsonResponseHandler {
            override def onFailure(jsonStream: JsonStream): Unit = {
               promise.complete(Success(NotFound(jsonStream)(new Writable[JsonStream] {
               })))
            }

            override def onSuccess(jsonStream: JsonStream): Unit = {

            }
          }
          rpcImplementation.incomingServiceProxy.apply(JsonStream.ARRAY(allParameters.iterator), resp)
        }
      }
    })

    promise.future
  }

  def rpc(uri: String) = Action.async { request =>
    val bodyJsonStream: JsonStream = JsonStream.STRING(request.body.asText.get)
    remoteCall(uri, bodyJsonStream)
  }
}
