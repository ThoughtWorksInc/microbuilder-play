package com.thoughtworks.microbuilder.play

import java.io.ByteArrayOutputStream

import jsonStream.io.{PrettyTextPrinter, TextParser}
import jsonStream.rpc.{IJsonResponseHandler, IJsonService}
import jsonStream.{JsonStream, JsonStreamPair}
import com.thoughtworks.microbuilder.core.{CoreSerializer, Failure => MicrobuilderFailure, IRouteConfiguration, IRouteEntry}
import haxe.io.Output
import play.api.http.Writeable
import play.api.libs.ws.{WSAPI, WSRequest}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class PlayOutgoingJsonService(urlPrefix: String, routes: IRouteConfiguration, wsAPI: WSAPI)(implicit executionContext: ExecutionContext) extends IJsonService {

  def prepareWSRequest(parameters: WrappedHaxeIterator[JsonStream], pair: JsonStreamPair): WSRequest = {
    val template: IRouteEntry = routes.nameToUriTemplate(pair.key)
    val request = wsAPI.url(urlPrefix + template.render(parameters.haxeIterator)).withMethod(template.get_method())
    val wsRequest = {
      if (parameters.hasNext) {
        val next: JsonStream = parameters.next()
        request.withBody(next)(new Writeable[JsonStream](
        {
          body =>
            val javaStream = new ByteArrayOutputStream()
            PrettyTextPrinter.print(new Output {
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
    wsRequest
  }

  def withSerializationExceptionHandling(responseHandler: IJsonResponseHandler, body: String)(func: () => Unit): Unit = {
    try {
      func()
    } catch {
      case e: Exception =>
        val serializationFailure = CoreSerializer.dynamicSerialize(haxe.root.ValueType.TEnum(classOf[MicrobuilderFailure]),
                                                                   MicrobuilderFailure.SERIALIZATION_FAILURE("Wrong Json format: " + body))
        responseHandler.onFailure(JsonStream.OBJECT(Iterator(serializationFailure)))
    }
  }

  def handleResponse(wsRequest: WSRequest, responseHandler: IJsonResponseHandler): Unit = {
    wsRequest.execute().onComplete { response =>
      response match {
        case Success(response) if response.status >= 200 && response.status < 400 =>
          withSerializationExceptionHandling(responseHandler, response.body) { () =>
            responseHandler.onSuccess(TextParser.parseString(response.body))
          }
        case Success(response) =>
          if (routes.get_failureClassName == null) {
            val textFailure = CoreSerializer.dynamicSerialize(haxe.root.ValueType.TEnum(classOf[MicrobuilderFailure]), MicrobuilderFailure.TEXT_APPLICATION_FAILURE(response.body))
            responseHandler.onFailure(JsonStream.OBJECT(Iterator(textFailure)))
          } else {
            withSerializationExceptionHandling(responseHandler, response.body) { () =>
              responseHandler.onFailure(
                JsonStream.OBJECT(
                  Iterator(
                    new JsonStreamPair(
                      "com.thoughtworks.microbuilder.core.Failure", JsonStream.OBJECT(
                        Iterator(
                          new JsonStreamPair(
                            "STRUCTURAL_APPLICATION_FAILURE", JsonStream.OBJECT(
                              Iterator(
                                new JsonStreamPair(
                                  "failure", JsonStream.OBJECT(
                                    Iterator(new JsonStreamPair(routes.get_failureClassName, TextParser.parseString(response.body)))
                                  )
                                )
                              )
                            )
                          )
                        )
                      )
                    )
                  )
                )
              )
            }
          }
        case Failure(e) =>
          val nativeFalure = CoreSerializer.dynamicSerialize(haxe.root.ValueType.TEnum(classOf[MicrobuilderFailure]), MicrobuilderFailure.NATIVE_FAILURE(e.getMessage))
          responseHandler.onFailure(JsonStream.OBJECT(Iterator(nativeFalure)))
      }

    }
  }

  def apply(requestParameters: JsonStream, responseHandler: IJsonResponseHandler): Unit = {
    requestParameters match {
      case JsonStreamExtractor.Object(pairs) =>
        val pair = pairs.next()
        pair.value match {
          case JsonStreamExtractor.Array(parameters) =>
            val wsRequest: WSRequest = prepareWSRequest(parameters, pair)

            handleResponse(wsRequest, responseHandler)
        }
      case _ =>
        throw new IllegalArgumentException("parameter should be an array")
    }
  }

  def push(x$1: jsonStream.JsonStream): Unit = {
  }

}
