package com.thoughtworks.restRpc.callee

import java.io.ByteArrayOutputStream

import com.qifun.jsonStream.JsonStream
import com.qifun.jsonStream.io.PrettyTextPrinter
import com.qifun.jsonStream.rpc.{IJsonResponseHandler, IJsonService}
import com.thoughtworks.restRpc.core.IRouteConfiguration
import haxe.io.Output
import play.api.http.Writeable
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

case class RpcEntry(routeConfiguration: IRouteConfiguration, incomingServiceProxy: IJsonService)


class MainController(rpcImplementations: Seq[RpcEntry]) extends Controller {

  def rpc(uri: String) = Action.async { request =>

    val bodyJsonStream:Option[JsonStream] = request.body.asText match {
      case None => None
      case Some(jsonStream) => Some(JsonStream.STRING(jsonStream))
    }

    val promise = Promise[Result]

    rpcImplementations.map(rpcImplementation => {
      println("{}{}{}")
      println(rpcImplementation.routeConfiguration.matchUri(request.method, uri, bodyJsonStream.getOrElse(null), request.contentType.getOrElse(null)).length)

      rpcImplementation.routeConfiguration.matchUri(request.method, uri, bodyJsonStream.getOrElse(null), request.contentType.getOrElse(null)) match {
        case null => {
          promise.success(NotFound)
          println("null")
        }
        case jsonStream: Array[JsonStream] => {

          println(jsonStream.length)

          val resp: IJsonResponseHandler = new IJsonResponseHandler {
            override def onFailure(jsonStream: JsonStream): Unit = {
              println("failed")
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
                println("success")
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

          rpcImplementation.incomingServiceProxy.apply(jsonStream(0), resp)
        }
      }
    })

    promise.future
  }
}
