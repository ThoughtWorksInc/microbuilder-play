package com.thoughtworks.microbuilder.play

import java.io.ByteArrayOutputStream

import com.thoughtworks.microbuilder.core.{MatchResult, IRouteConfiguration}
import haxe.io.Output
import haxe.lang.HaxeException
import jsonStream.{JsonDeserializer, JsonStream}
import jsonStream.io.PrettyTextPrinter
import jsonStream.rpc.{IJsonResponseHandler, IJsonService}
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
              play.api.Logger.error(JsonDeserializer.deserializeRaw(jsonStream).toString)
              /*
              {
                com.thoughtworks.microbuilder.core.Failure : {
                  TEXT_APPLICATION_FAILURE : {
                    message : app exception,
                    status : 507
                  }
                }
              }
                  */
              jsonStream match {
                case JsonStreamExtractor.Object(pairs) =>
                  if (pairs.hasNext) {
                    val pair = pairs.next()
                    pair.key match {
                      case "TEXT_APPLICATION_FAILURE" =>
                        pair.value match {
                          case JsonStreamExtractor.Object(subpairs) =>
                            var messageOption: Option[String] = None
                            var statusOption: Option[Int] = None
                            for (subpair <- subpairs) {
                              subpair.key match {
                                case "message" =>
                                  subpair.value match {
                                    case JsonStreamExtractor.String(messageValue) =>
                                      messageOption = Some(messageValue)
                                  }
                                case "status" =>
                                  subpair.value match {
                                    case JsonStreamExtractor.Int32(statusValue) =>
                                      statusOption = Some(statusValue)
                                    case JsonStreamExtractor.Number(statusValue) =>
                                      statusOption = Some(statusValue.toInt)
                                  }
                              }
                            }
                            val Some(status) = statusOption
                            val Some(message) = messageOption
                            promise.success(new Status(status)(message))
                        }
                      case "STRUCTURAL_APPLICATION_FAILURE" =>
                        pair.value match {
                          case JsonStreamExtractor.Object(subpairs) =>
                            var failureOption: Option[ByteArrayOutputStream] = None
                            var statusOption: Option[Int] = None
                            for (subpair <- subpairs) {
                              subpair.key match {
                                case "failure" =>
                                  val byteStream = new ByteArrayOutputStream()
                                  PrettyTextPrinter.print(new Output {
                                    override def writeByte(b: Int) = {
                                      byteStream.write(b)
                                    }
                                  }, jsonStream, 0)
                                  Some(byteStream)
                                case "status" =>
                                  subpair.value match {
                                    case JsonStreamExtractor.Int32(statusValue) =>
                                      statusOption = Some(statusValue)
                                    case JsonStreamExtractor.Number(statusValue) =>
                                      statusOption = Some(statusValue.toInt)
                                  }
                              }
                            }
                            val Some(status) = statusOption
                            val Some(byteStream) = failureOption
                            promise.success(new Status(status)(byteStream)(new Writeable[ByteArrayOutputStream]({ jsonStream =>
                              byteStream.toByteArray
                            }, Some(rpcImplementation.routeConfiguration.get_failureResponseContentType))))
                        }
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
}
