package com.thoughtworks.restRpc.play

import com.qifun.jsonStream.rpc.{ICompleteHandler1, IFuture1}
import com.thoughtworks.restRpc.play.exception.RestRpcException.{StructuralApplicationException, TextApplicationException}

import scala.concurrent.{Promise, Future}
import com.thoughtworks.restRpc.core.{Failure => RestRpcFailure}
import scala.concurrent.ExecutionContext.Implicits.global
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
        val failure = obj.asInstanceOf[RestRpcFailure]
        //TODO： 不要使用getTag
        failure.getTag match {
            // TODO: NativeException
          case "TEXT_APPLICATION_FAILURE" =>
            p failure new TextApplicationException(haxe.root.Type.enumParameters(failure).__get(0).asInstanceOf[String])
          case "STRUCTURAL_APPLICATION_FAILURE" =>
            p failure new StructuralApplicationException(haxe.root.Type.enumParameters(failure).__get(0))
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
              case StructuralApplicationException(data) => {
                handler.onFailure(RestRpcFailure.STRUCTURAL_APPLICATION_FAILURE(data))
              }
              case TextApplicationException(reason) => {
                handler.onFailure(RestRpcFailure.TEXT_APPLICATION_FAILURE(reason))
              }
            }
          }
          case Success(value) => handler.onSuccess(value)
        }
      }
    }
  }
}
