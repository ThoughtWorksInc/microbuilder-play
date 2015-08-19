package com.thoughtworks.restRpc.play

import com.thoughtworks.restRpc.core.IUriTemplate

class FakeUriTemplate extends IUriTemplate{
  override def get_method(): String = "GET"

  override def render(parameters: scala.Any): String = {
    //    "/my-method/{p1}/name/{name}"
    var url = "/my-method/"
    parameters match {
      case JsonStreamExtractor.Object(pairs) =>
        pairs.next().value match {
          case JsonStreamExtractor.String(stringValue) =>
            url += stringValue
        }
        url += "/name"
        pairs.next().value match {
          case JsonStreamExtractor.String(stringValue) =>
            url += stringValue
        }
    }
    url
  }
}
