package com.thoughtworks.microbuilder.play

import com.thoughtworks.microbuilder.core.IRouteConfiguration
import jsonStream.rpc.IJsonService
import scala.language.experimental.macros

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
trait IncomingStub[Service] {

  def routeConfiguration: IRouteConfiguration

  def incomingServiceProxy(serviceImplementation: Service): IJsonService
}

object IncomingStub {

  implicit def apply[Service]: IncomingStub[Service] = macro Macros.newIncomingStub

}