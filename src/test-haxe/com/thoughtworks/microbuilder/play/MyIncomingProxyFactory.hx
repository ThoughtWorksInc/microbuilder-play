package com.thoughtworks.microbuilder.play;

using jsonStream.Plugins;
using com.thoughtworks.microbuilder.play.MySerializer;
using com.thoughtworks.microbuilder.play.MyDeserializer;


@:build(jsonStream.rpc.IncomingProxyFactory.generateIncomingProxyFactory([
  "com.thoughtworks.microbuilder.play.MyRpc",
  "com.thoughtworks.microbuilder.play.MyRpcWithStructuralException",
  "com.thoughtworks.microbuilder.play.ICustomHeader",
]))
class MyIncomingProxyFactory {}
