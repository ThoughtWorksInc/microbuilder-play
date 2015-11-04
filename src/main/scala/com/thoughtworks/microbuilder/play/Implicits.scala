package com.thoughtworks.microbuilder.play

import jsonStream.rpc.{ICompleteHandler1, IFuture1}
import com.thoughtworks.microbuilder.core.{Failure => MicrobuilderFailure}
import com.thoughtworks.microbuilder.play.exception.MicrobuilderException._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util._

object Implicits {

  private val FailureConstructors = haxe.root.Type.getEnumConstructs(classOf[MicrobuilderFailure])

  private val TextApplicationFailureIndex: Int = FailureConstructors.indexOf("TEXT_APPLICATION_FAILURE", 0)
  private val StructuralApplicationFailureIndex: Int = FailureConstructors.indexOf("STRUCTURAL_APPLICATION_FAILURE", 0)
  private val SerializationFailureIndex: Int = FailureConstructors.indexOf("SERIALIZATION_FAILURE", 0)


  implicit def jsonStreamFutureToScalaFuture[Value](jsonStreamFuture: IFuture1[Value]): Future[Value] = {
    val p = Promise[Value]()

    jsonStreamFuture.start(new ICompleteHandler1[Value] {
      override def onSuccess(value: Value): Unit = p success value

      override def onFailure(obj: scala.Any): Unit = {
        val failure = obj.asInstanceOf[MicrobuilderFailure]
        haxe.root.Type.enumIndex(obj) match {
          case TextApplicationFailureIndex =>
            p failure new TextApplicationException(
              haxe.root.Type.enumParameters(failure).__get(0).asInstanceOf[String],
              haxe.root.Type.enumParameters(failure).__get(1).asInstanceOf[Int]
            )
          case StructuralApplicationFailureIndex =>
            p failure new StructuralApplicationException(
              haxe.root.Type.enumParameters(failure).__get(0),
              haxe.root.Type.enumParameters(failure).__get(1).asInstanceOf[Int]
            )
          case SerializationFailureIndex =>
            p failure new WrongResponseFormatException(
              haxe.root.Type.enumParameters(failure).__get(0).asInstanceOf[String]
            )
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
              case StructuralApplicationException(data, status) => {
                handler.onFailure(MicrobuilderFailure.STRUCTURAL_APPLICATION_FAILURE(data, status))
              }
              case TextApplicationException(reason, status) => {
                handler.onFailure(MicrobuilderFailure.TEXT_APPLICATION_FAILURE(reason, status))
              }
            }
          }
          case Success(value) => handler.onSuccess(value)
        }
      }
    }
  }
}
