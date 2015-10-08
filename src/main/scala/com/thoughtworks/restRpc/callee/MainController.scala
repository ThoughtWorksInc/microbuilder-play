package com.thoughtworks.restRpc.callee

import java.io.ByteArrayOutputStream

import com.qifun.jsonStream.JsonStream
import com.qifun.jsonStream.io.PrettyTextPrinter
import com.qifun.jsonStream.rpc.{IJsonResponseHandler, IJsonService}
import com.thoughtworks.restRpc.core.IRouteConfiguration
import haxe.io.Output
import play.api.mvc._
import play.api.http.Writeable
import scala.concurrent.Promise
import scala.concurrent.ExecutionContext.Implicits.global

case class RpcEntry(routeConfiguration: IRouteConfiguration, incomingServiceProxy: IJsonService)


class MainController(rpcImplementations: Seq[RpcEntry]) extends Controller {

  def rpc(uri: String) = Action.async { request =>

    val bodyJsonStream: JsonStream = JsonStream.STRING(request.body.asText.get)

    val promise = Promise[Result]

    rpcImplementations.map(rpcImplementation => {
      rpcImplementation.routeConfiguration.matchUri(request.method, uri, bodyJsonStream, request.contentType.getOrElse(null)) match {
        case null => promise.success(NotFound)
        case jsonStream: Array[JsonStream] => {

          val resp: IJsonResponseHandler = new IJsonResponseHandler {
            override def onFailure(jsonStream: JsonStream): Unit = {
              promise success Ok(jsonStream)(new Writeable[JsonStream]({ jsonStream =>
                val javaStream = new ByteArrayOutputStream()
                PrettyTextPrinter.print(new Output {
                  override def writeByte(b: Int) = {
                    javaStream.write(b)
                  }
                }, jsonStream, 0)
                javaStream.toByteArray
              }, Some("application/JsonStream")))
            }

            override def onSuccess(jsonStream: JsonStream): Unit = {
              promise success (Ok(jsonStream)(new Writeable[JsonStream]({ jsonStream =>
                val javaStream = new ByteArrayOutputStream()
                PrettyTextPrinter.print(new Output {
                  override def writeByte(b: Int) = {
                    javaStream.write(b)
                  }
                }, jsonStream, 0)

                javaStream.toByteArray
              }, Some("application/JsonStream"))))
            }
          }

          rpcImplementation.incomingServiceProxy.apply(JsonStream.ARRAY(jsonStream.iterator), resp)
        }
      }
    })

    promise.future
  }
}
