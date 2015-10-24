package com.thoughtworks.microbuilder.play;

using com.qifun.jsonStream.Plugins;
using com.thoughtworks.microbuilder.play.MySerializer;
using com.thoughtworks.microbuilder.play.MyDeserializer;


@:build(com.qifun.jsonStream.rpc.IncomingProxyFactory.generateIncomingProxyFactory([
  "com.thoughtworks.microbuilder.play.MyRpc"
]))
class MyIncomingProxyFactory {}