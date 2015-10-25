package com.thoughtworks.microbuilder.play;

using jsonStream.Plugins;
using com.thoughtworks.microbuilder.play.MyDeserializer;
using com.thoughtworks.microbuilder.play.MySerializer;


@:build(jsonStream.rpc.OutgoingProxyFactory.generateOutgoingProxyFactory([
  "com.thoughtworks.microbuilder.play.MyRpc",
  "com.thoughtworks.microbuilder.play.MyRpcWithStructuralException"
]))
class MyOutgoingProxyFactory {}