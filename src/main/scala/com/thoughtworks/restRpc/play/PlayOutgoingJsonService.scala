package com.thoughtworks.restRpc.play

import java.io.ByteArrayOutputStream

import com.qifun.jsonStream.io.{PrettyTextPrinter, TextParser}
import com.qifun.jsonStream.rpc.{IJsonResponseHandler, IJsonService}
import com.qifun.jsonStream.{JsonStream, JsonStreamPair}
import com.thoughtworks.restRpc.core.{CoreSerializer, Failure => RestRpcFailure, IRouteConfiguration, IUriTemplate}
import haxe.io.Output
import play.api.http.Writeable
import play.api.libs.ws.{WSAPI, WSRequest}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}



class PlayOutgoingJsonService(urlPrefix: String, routes: IRouteConfiguration, wsAPI: WSAPI)(implicit executionContext: ExecutionContext) extends IJsonService {

  def prepareWSRequest(parameters: WrappedHaxeIterator[JsonStream], pair: JsonStreamPair): WSRequest = {
    val template: IUriTemplate = routes.nameToUriTemplate(pair.key)
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

  def handleResponse(wsRequest: WSRequest, responseHandler: IJsonResponseHandler): Unit = {
    wsRequest.execute().onComplete {
      case Success(response) if response.status >= 200 && response.status < 400 =>
        responseHandler.onSuccess(TextParser.parseString(response.body))
      case Success(response) =>
        if (routes.get_failureClassName == null) {
          val testFailure = CoreSerializer.dynamicSerialize(haxe.root.ValueType.TEnum(classOf[RestRpcFailure]), RestRpcFailure.TEXT_APPLICATION_FAILURE(response.body))
          responseHandler.onFailure(JsonStream.OBJECT(Iterator(testFailure)))
        } else {
          //TODO：处理返回的不是json的这种情况
          responseHandler.onFailure(
            JsonStream.OBJECT(
              Iterator(
                new JsonStreamPair(
                  "com.thoughtworks.restRpc.core.Failure", JsonStream.OBJECT(
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
      case Failure(e) =>
        //NATIVE_FAILURE
        responseHandler.onFailure(JsonStream.STRING(e.getMessage))
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

  def push(x$1: com.qifun.jsonStream.JsonStream): Unit = {
  }

}
