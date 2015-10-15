package com.thoughtworks.restRpc.play

import java.io.ByteArrayOutputStream

import com.qifun.jsonStream.{JsonStreamPair, JsonStream}
import com.qifun.jsonStream.io.{TextParser, PrettyTextPrinter}
import com.qifun.jsonStream.rpc.{IJsonResponseHandler, ICompleteHandler1, IFuture1, IJsonService}
import com.thoughtworks.restRpc.core.{CoreSerializer, Failure => RestRpcFailure, IRouteConfiguration, IUriTemplate}
import com.thoughtworks.restRpc.play.exception.RestRpcException._
import haxe.io.Output
import play.api.http.Writeable
import play.api.libs.ws.{WSAPI, WSRequest}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

object Implicits {

  implicit def jsonStreamFutureToScalaFuture[Value](jsonStreamFuture: IFuture1[Value]): Future[Value] = {
    val p = Promise[Value]()

    jsonStreamFuture.start(new ICompleteHandler1[Value] {
      override def onSuccess(value: Value): Unit = p success value

      override def onFailure(obj: scala.Any): Unit = {
        val failure = obj.asInstanceOf[RestRpcFailure]
        //TODO： 不要使用getTag
        failure.getTag match {
          case "TEXT_APPLICATION_FAILURE" =>
            p failure new TextApplicationException(haxe.root.Type.enumParameters(failure).__get(0).asInstanceOf[String])
          case "STRUCTURAL_APPLICATION_FAILURE" =>
            p failure new StructuralApplicationException(haxe.root.Type.enumParameters(failure).__get(0))
          case "SERIALIZATION_FAILURE" =>
            p failure new WrongResponseFormatException(haxe.root.Type.enumParameters(failure).__get(0).asInstanceOf[String])
        }
      }
    })

    p.future
  }
}

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

  def withSerializationExceptionHandling(responseHandler: IJsonResponseHandler, body: String)(func: () => Unit): Unit = {
    try {
      func()
    } catch {
      case e: Exception =>
        val serializationFailure = CoreSerializer.dynamicSerialize(haxe.root.ValueType.TEnum(classOf[RestRpcFailure]),
                                                                   RestRpcFailure.SERIALIZATION_FAILURE("Wrong Json format: " + body))
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
            val textFailure = CoreSerializer.dynamicSerialize(haxe.root.ValueType.TEnum(classOf[RestRpcFailure]), RestRpcFailure.TEXT_APPLICATION_FAILURE(response.body))
            responseHandler.onFailure(JsonStream.OBJECT(Iterator(textFailure)))
          } else {
            withSerializationExceptionHandling(responseHandler, response.body) { () =>
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
          }
        case Failure(e) =>
          val nativeFalure = CoreSerializer.dynamicSerialize(haxe.root.ValueType.TEnum(classOf[RestRpcFailure]), RestRpcFailure.NATIVE_FAILURE(e.getMessage))
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

  def push(x$1: com.qifun.jsonStream.JsonStream): Unit = {
  }

}
