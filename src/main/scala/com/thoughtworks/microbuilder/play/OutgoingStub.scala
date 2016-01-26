package com.thoughtworks.microbuilder.play

import com.thoughtworks.microbuilder.core.IRouteConfiguration
import jsonStream.rpc.IJsonService
import scala.language.experimental.macros

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
trait OutgoingStub[Service] {

  def routeConfiguration: IRouteConfiguration

  def outgoingServiceProxy(jsonService: IJsonService): Service

}

object OutgoingStub {

  implicit def apply[Service]: OutgoingStub[Service] = macro Macros.newOutgoingStub

}