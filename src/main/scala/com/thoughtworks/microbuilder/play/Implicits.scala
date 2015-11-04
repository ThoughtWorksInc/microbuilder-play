package com.thoughtworks.microbuilder.play

import jsonStream.rpc.{ICompleteHandler1, IFuture1}
import com.thoughtworks.microbuilder.core.{Failure => MicrobuilderFailure}
import com.thoughtworks.microbuilder.play.exception.MicrobuilderException._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util._

/**
 * Created by zwshao on 10/12/15.
 */
object Implicits {

  implicit def jsonStreamFutureToScalaFuture[Value](jsonStreamFuture: IFuture1[Value]): Future[Value] = {
    val p = Promise[Value]()

    jsonStreamFuture.start(new ICompleteHandler1[Value] {
      override def onSuccess(value: Value): Unit = p success value

      override def onFailure(obj: scala.Any): Unit = {
        val failure = obj.asInstanceOf[MicrobuilderFailure]
        val params = haxe.root.Type.enumParameters(failure)
        //TODO： 不要使用getTag
        failure.getTag match {
          case "TEXT_APPLICATION_FAILURE" =>
            p failure new TextApplicationException(params.__get(0).asInstanceOf[String], params.__get(1).asInstanceOf[Int])
          case "STRUCTURAL_APPLICATION_FAILURE" =>
            p failure new StructuralApplicationException(params.__get(0), params.__get(1).asInstanceOf[Int])
          case "SERIALIZATION_FAILURE" =>
            p failure new WrongResponseFormatException(params.__get(0).asInstanceOf[String])
          case "NATIVE_FAILURE" =>
            p failure new NativeException(params.__get(0).asInstanceOf[String])
        }
      }
    })

    p.future
  }

  implicit def scalaFutureToJsonStreamFuture[Value](future: Future[Value]): IFuture1[Value] = {

    new IFuture1[Value] {
      override def start(handler: ICompleteHandler1[Value]): Unit = {
        future.onComplete {
          case Failure(e) => {
            e match {
              // TODO: NativeException
              case StructuralApplicationException(data, code) => {
                handler.onFailure(MicrobuilderFailure.STRUCTURAL_APPLICATION_FAILURE(data, code))
              }
              case TextApplicationException(reason, code) => {
                handler.onFailure(MicrobuilderFailure.TEXT_APPLICATION_FAILURE(reason, code))
              }
            }
          }
          case Success(value) => handler.onSuccess(value)
        }
      }
    }
  }
}
