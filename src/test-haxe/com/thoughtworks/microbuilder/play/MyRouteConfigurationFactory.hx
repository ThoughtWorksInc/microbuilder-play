package com.thoughtworks.microbuilder.play;


@:build(com.thoughtworks.microbuilder.core.RouteConfigurationFactory.generateRouteConfigurationFactory([
  "com.thoughtworks.microbuilder.play.MyRpc",
  "com.thoughtworks.microbuilder.play.MyRpcWithStructuralException",
  "com.thoughtworks.microbuilder.play.ICustomHeader",
]))
class MyRouteConfigurationFactory {}
