package com.thoughtworks.microbuilder.play

import java.io.ByteArrayOutputStream

import com.thoughtworks.microbuilder.core.{IRouteConfiguration, MatchResult}
import haxe.io.Output
import haxe.lang.HaxeException
import jsonStream.io.PrettyTextPrinter
import jsonStream.rpc.{IJsonResponseHandler, IJsonService}
import jsonStream.{JsonStream, JsonStreamPair}
import play.api.http.Writeable
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Promise

case class RpcEntry(routeConfiguration: IRouteConfiguration, incomingServiceProxy: IJsonService)


class MainController(rpcImplementations: Seq[RpcEntry]) extends Controller {

  def rpc(uri: String) = Action.async { request =>

    val bodyJsonStream: Option[JsonStream] = request.body.asText match {
      case None => None
      case Some(jsonStream) => Some(JsonStream.STRING(jsonStream))
    }

    val promise = Promise[Result]

    rpcImplementations.map(rpcImplementation => {

      rpcImplementation.routeConfiguration.matchUri(request.method, uri, bodyJsonStream.getOrElse(null), request.contentType.getOrElse(null)) match {
        case null => {
          promise.success(NotFound(s"URL $uri does not match."))
        }
        case matchResult: MatchResult => {
          val jsonStream = matchResult.rpcData
          val resp: IJsonResponseHandler = new IJsonResponseHandler {
            override def onFailure(jsonStream: JsonStream): Unit = {

              /*
              {
                com.thoughtworks.microbuilder.core.Failure : {
                  STRUCTURAL_APPLICATION_FAILURE : {
                    failure : {
                        String : app structural exception
                    },
                    status : 507
                  }
                }
              }
               */
              /*{
                com.thoughtworks.microbuilder.core.Failure : {
                  TEXT_APPLICATION_FAILURE : {
                    message : app exception,
                    status : 507
                  }
                }
              }*/
              jsonStream match {
                case JsonStreamExtractor.Object(pairs) =>

                  if (pairs.hasNext) {
                    val pair = pairs.next()

                    pair.value match {
                      case JsonStreamExtractor.Object(appErrorInfoPair) =>
                        if (appErrorInfoPair.hasNext) {
                          val appErrorInfo = appErrorInfoPair.next()
                          appErrorInfo.key match {
                            case "TEXT_APPLICATION_FAILURE" =>
                              writeOutTextErrorInfo(promise, appErrorInfo)
                            case "STRUCTURAL_APPLICATION_FAILURE" =>
                              writeOutTextErrorInfo(promise, appErrorInfo)
                            case _ =>
                              throw new IllegalStateException("failure must contain one key/value pair.")
                          }
                        } else {
                          throw new IllegalStateException("failure must contain one key/value pair.")
                        }
                      case _ =>
                        throw new IllegalStateException("failure must contain one key/value pair.")
                    }
                  } else {
                    throw new IllegalStateException("failure must contain one key/value pair.")
                  }
                  if (pairs.hasNext) {
                    throw new IllegalStateException("failure must contain only one key/value pair.")
                  }
              }
            }

            override def onSuccess(jsonStream: JsonStream): Unit = {
              promise.success(Ok(jsonStream)(new Writeable[JsonStream]({ jsonStream =>
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

          try {
            rpcImplementation.incomingServiceProxy.apply(jsonStream, resp)
          } catch {
            // FIXME: Should handle different exceptions.
            case e: HaxeException => {
              play.api.Logger.warn(e.getObject.toString, e)
              promise.success(BadRequest(s"Cannot parse request for $uri"))
            }
          }
        }
      }
    })

    promise.future
  }
  
  def writeOutTextErrorInfo(promise: Promise[Result], appErrorInfo: JsonStreamPair): promise.type = {
    appErrorInfo.value match {
      case JsonStreamExtractor.Object(subPairs) =>
        var messageOption: Option[String] = None
        var statusOption: Option[Int] = None
        for (subPair <- subPairs) {
          subPair.key match {
            case "message" =>
              subPair.value match {
                case JsonStreamExtractor.String(messageValue) =>
                  messageOption = Some(messageValue)
              }
            case "status" =>
              subPair.value match {
                case JsonStreamExtractor.Int32(statusValue) =>
                  statusOption = Some(statusValue)
                case JsonStreamExtractor.Number(statusValue) =>
                  statusOption = Some(statusValue.toInt)
              }
            case "failure" =>
              subPair.value match {
                case JsonStreamExtractor.Object(messageValue) =>
                  if(messageValue.hasNext){
                    val failureValue = messageValue.next()
                    failureValue.value match {
                      case JsonStreamExtractor.String(failureString) =>
                        messageOption = Some(failureString)
                    }
                  }
              }
          }
        }
        val Some(status) = statusOption
        val Some(message) = messageOption
        promise.success(new Status(status)(message))
    }
  }
}
