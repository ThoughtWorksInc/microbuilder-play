package com.thoughtworks.microbuilder.play.exception

sealed trait MicrobuilderException extends Exception

object MicrobuilderException {

  sealed trait ApplicationException extends MicrobuilderException

  final case class StructuralApplicationException[A](data: A, status: Int) extends Exception with ApplicationException

  final case class TextApplicationException(reason: String, status: Int) extends Exception(reason) with ApplicationException

  final case class NativeException(reason: String) extends Exception(reason) with MicrobuilderException

  final case class WrongResponseFormatException(reason: String) extends Exception with MicrobuilderException

}