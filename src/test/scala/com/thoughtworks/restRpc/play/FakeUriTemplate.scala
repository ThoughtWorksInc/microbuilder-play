package com.thoughtworks.restRpc.play

import com.thoughtworks.restRpc.core.IUriTemplate

class FakeUriTemplate extends IUriTemplate {
  override def get_method(): String = "GET"

  override def render(parameters: scala.AnyRef): String = {
    val parametersIterator = WrappedHaxeIterator(parameters)
    var url = new StringBuilder
    url ++= "/my-method/"
    parametersIterator.next() match {
      case JsonStreamExtractor.Int32(number) =>
        url ++= number.toString
    }
    url ++= "/name/"
    parametersIterator.next() match {
      case JsonStreamExtractor.String(stringValue) =>
        url ++= stringValue
        url.toString
    }
  }
}