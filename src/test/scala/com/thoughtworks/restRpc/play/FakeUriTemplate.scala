package com.thoughtworks.restRpc.play

import com.qifun.jsonStream.JsonStream
import com.thoughtworks.restRpc.core.IUriTemplate
import haxe.root.Reflect

class FakeUriTemplate(methodName: String, resultUrl: String, numOfUrlParams:Int) extends IUriTemplate {
  override def get_method(): String = methodName
  override def render(parameters: scala.AnyRef): String = {
    for(_ <- 1 to numOfUrlParams) {
      Reflect.callMethod(parameters, Reflect.field(parameters, "next"), new haxe.root.Array())
    }
    resultUrl
  }

  override def parseUri(uri: String): Array[JsonStream] = throw new Exception("didn't implemented")

  override def get_requestContentType(): String = throw new Exception("didn't implemented")
}