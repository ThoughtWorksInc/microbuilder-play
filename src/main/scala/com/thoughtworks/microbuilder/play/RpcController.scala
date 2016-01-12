package com.thoughtworks.microbuilder.play

import java.io.ByteArrayOutputStream

import com.thoughtworks.microbuilder.core.{IRouteConfiguration, MatchResult}
import com.thoughtworks.microbuilder.play.exception.MicrobuilderException.{WrongResponseFormatException, NativeException}
import haxe.io.Output
import haxe.lang.HaxeException
import jsonStream.io.PrettyTextPrinter
import jsonStream.rpc.{IJsonResponseHandler, IJsonService}
import jsonStream.{JsonStream, JsonStreamPair}
import play.api.http.Writeable
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.Try

case class RpcEntry(routeConfiguration: IRouteConfiguration, incomingServiceProxy: IJsonService)


class RpcController(rpcEntries: Seq[RpcEntry]) extends Controller {

  def rpc(uri: String) = Action.async { request =>
    val bodyJsonStream: Option[JsonStream] = request.body.asText match {
      case None => None
      case Some(jsonStream) => Some(JsonStream.STRING(jsonStream))
    }

    val matchedEntries = for {
      rpcEntry <- rpcEntries.iterator
      matchResult = rpcEntry.routeConfiguration.matchUri(request.method, uri, bodyJsonStream.getOrElse(null), request.contentType.getOrElse(null))
      if (matchResult != null)
    } yield (rpcEntry, matchResult)

    if (matchedEntries.hasNext) {
      val (rpcEntry, matchResult) = matchedEntries.next()


      try {
        val promise = Promise[Result]
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
                    case JsonStreamExtractor.Object(failurePairs) =>
                      if (failurePairs.hasNext) {
                        val failurePair = failurePairs.next()
                        val expectedFailure = rpcEntry.routeConfiguration.get_failureClassName
                        failurePair.key match {
                          case "TEXT_APPLICATION_FAILURE" =>
                            if (expectedFailure == null) {
                              promise.success(textFailureResult(failurePair.value))
                            } else {
                              promise.failure(new WrongResponseFormatException(s"Expect a $expectedFailure, actually TEXT_APPLICATION_FAILURE(${PrettyTextPrinter.toString(failurePair.value)})"))
                            }
                          case "STRUCTURAL_APPLICATION_FAILURE" =>
                            if (expectedFailure != null) {
                              promise.complete(Try(structuralFailureResult(Option(matchResult.routeEntry.get_responseContentType), rpcEntry.routeConfiguration.get_failureClassName, failurePair.value)))
                            } else {
                              promise.failure(new WrongResponseFormatException(s"Expect text, actually STRUCTURAL_APPLICATION_FAILURE(${PrettyTextPrinter.toString(failurePair.value)})"))
                            }
                          case "NATIVE_FAILURE" =>
                            promise.failure(nativeFailureException(failurePair.value))
                          case _ =>
                            promise.failure( new IllegalStateException("failure must be a Failure."))
                        }
                      } else {
                        promise.failure(new IllegalStateException("failure must contain one key/value pair."))
                      }
                    case _ =>
                      promise.failure( new IllegalStateException("failure must be a JSON object."))
                  }
                } else {
                  promise.failure( new IllegalStateException("failure must contain one key/value pair."))
                }
                if (pairs.hasNext) {
                  promise.failure( new IllegalStateException("failure must contain only one key/value pair."))
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
        rpcEntry.incomingServiceProxy.apply(matchResult.rpcData, resp)
        promise.future
      } catch {
        // FIXME: Should handle different exceptions.
        case e: HaxeException => {
          play.api.Logger.warn(e.getObject.toString, e)
          Future.successful(BadRequest(s"Cannot parse request for $uri"))
        }
      }
    } else {
      Future.successful(NotFound(s"URL $uri does not match."))
    }
  }

  private def nativeFailureException(nativeFailureStream: JsonStream): NativeException = {
    nativeFailureStream match {
      case JsonStreamExtractor.Object(subPairs) =>
        var messageOption: Option[String] = None
        for (subPair <- subPairs) {
          subPair.key match {
            case "message" =>
              subPair.value match {
                case JsonStreamExtractor.String(messageValue) =>
                  messageOption = Some(messageValue)
              }
          }
        }
        val Some(message) = messageOption
        new NativeException(message)
    }
  }

  private def textFailureResult(textApplicationFailureStream: JsonStream): Result = {
    textApplicationFailureStream match {
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
          }
        }
        val Some(status) = statusOption
        val Some(message) = messageOption
        new Status(status)(message)
    }
  }

  private def structuralFailureResult(contentType: Option[String], failureName: String, structuralApplicationFailureStream: JsonStream): Result = {
    structuralApplicationFailureStream match {
      case JsonStreamExtractor.Object(subPairs) =>
        var failureStreamOption: Option[Array[Byte]] = None
        var statusOption: Option[Int] = None
        for (subPair <- subPairs) {
          subPair.key match {
            case "status" =>
              subPair.value match {
                case JsonStreamExtractor.Int32(statusValue) =>
                  statusOption = Some(statusValue)
                case JsonStreamExtractor.Number(statusValue) =>
                  statusOption = Some(statusValue.toInt)
              }
            case "failure" =>
              subPair.value match {
                case JsonStreamExtractor.Object(failurePairs) => {
                  if (failurePairs.hasNext) {
                    val failurePair = failurePairs.next()
                    if (failurePair.key == failureName) {
                      failureStreamOption = Some(PrettyTextPrinter.toString(failurePair.value).getBytes)
                    } else {
                      throw new IllegalArgumentException(s"Failure type name does not match. Expect $failureName, actually ${failurePair.key}")
                    }
                    if (failurePairs.hasNext) {
                      throw new IllegalArgumentException("Failure JSON must contain one key/value pair.")
                    }
                  } else {
                    throw new IllegalArgumentException("Failure JSON must contain one key/value pair.")
                  }
                }
              }
          }
        }
        val Some(status) = statusOption
        val Some(failureStream) = failureStreamOption
        new Status(status)(failureStream)(Writeable[Array[Byte]](locally[Array[Byte]](_), contentType))
    }
  }
}
