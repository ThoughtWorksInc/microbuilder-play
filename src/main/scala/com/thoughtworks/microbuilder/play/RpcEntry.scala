package com.thoughtworks.microbuilder.play

import com.thoughtworks.microbuilder.core.IRouteConfiguration
import jsonStream.rpc.IJsonService

case class RpcEntry(routeConfiguration: IRouteConfiguration, incomingServiceProxy: IJsonService)
