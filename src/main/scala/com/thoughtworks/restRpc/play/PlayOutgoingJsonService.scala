package com.thoughtworks.restRpc.play

import java.io.ByteArrayOutputStream

import com.qifun.jsonStream.JsonStream
import com.qifun.jsonStream.io.{PrettyTextPrinter, TextParser}
import com.qifun.jsonStream.rpc.IJsonService
import com.thoughtworks.restRpc.core.{IRouteConfiguration, IUriTemplate}
import play.api.http.Writeable
import play.api.libs.ws.WSAPI

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class PlayOutgoingJsonService(urlPrefix: String, routes: IRouteConfiguration, wsAPI: WSAPI)(implicit executionContext: ExecutionContext) extends IJsonService {

  def apply(requestParameters: com.qifun.jsonStream.JsonStream, responseHandler: com.qifun.jsonStream.rpc.IJsonResponseHandler): Unit = {
    requestParameters match {
      case JsonStreamExtractor.Object(pairs) =>
        val pair = pairs.next()
        pair.value match {
          case JsonStreamExtractor.Array(parameters) =>
            val template: IUriTemplate = routes.nameToUriTemplate(pair.key)
            val request = wsAPI.url(urlPrefix + template.render(parameters.haxeIterator)).withMethod(template.get_method())
            val wsRequest = {
              if (parameters.hasNext) {
                val next: JsonStream = parameters.next()
                request.withBody(next)(new Writeable[JsonStream](
                {
                  body =>
                    val javaStream = new ByteArrayOutputStream()
                    PrettyTextPrinter.print(new haxe.io.Output {
                      override def writeByte(b: Int) = {
                        javaStream.write(b)
                      }
                    }, body, 0)
                    javaStream.toByteArray
                },
                Some("application/json")))
              } else {
                request
              }
            }
            wsRequest.execute().onComplete {
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

def push (x$1: com.qifun.jsonStream.JsonStream): Unit = {
}

}
