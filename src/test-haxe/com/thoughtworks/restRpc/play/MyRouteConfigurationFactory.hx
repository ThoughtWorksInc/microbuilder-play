package com.thoughtworks.restRpc.play;


@:build(com.thoughtworks.restRpc.core.RouteConfigurationFactory.generateRouteConfigurationFactory([
  "com.thoughtworks.restRpc.play.MyRpc"
]))
class MyRouteConfigurationFactory {}