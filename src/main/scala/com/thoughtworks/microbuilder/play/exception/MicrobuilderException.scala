package com.thoughtworks.microbuilder.play.exception

sealed trait MicrobuilderException extends Exception

object MicrobuilderException {

  final case class StructuralApplicationException[A](data: A) extends Exception with MicrobuilderException

  final case class TextApplicationException(reason: String) extends Exception(reason) with MicrobuilderException

  final case class NativeException(reason: String) extends Exception(reason) with MicrobuilderException

  final case class WrongResponseFormatException(reason: String) extends Exception with MicrobuilderException
}