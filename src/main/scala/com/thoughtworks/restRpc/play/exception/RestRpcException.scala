package com.thoughtworks.restRpc.play.exception

sealed trait RestRpcException extends Exception

object RestRpcException {

  final case class StructuralApplicationException[A](data:A) extends Exception with RestRpcException

  final case class TextApplicationException(reason:String) extends Exception(reason) with RestRpcException

  final case class NativeException(cause: Throwable) extends Exception(cause) with RestRpcException

}