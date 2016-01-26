package com.thoughtworks.microbuilder.play

import com.thoughtworks.microbuilder.core.IRouteConfiguration
import jsonStream.rpc.IJsonService

case class RpcEntry(routeConfiguration: IRouteConfiguration, incomingServiceProxy: IJsonService)

object RpcEntry {
  def implementedBy[Service](serviceImplementation: Service)(implicit stub: IncomingStub[Service]) = {
    RpcEntry(stub.routeConfiguration, stub.incomingServiceProxy(serviceImplementation))
  }
}