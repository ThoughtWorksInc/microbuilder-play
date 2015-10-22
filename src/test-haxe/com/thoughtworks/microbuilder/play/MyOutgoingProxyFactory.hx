package com.thoughtworks.microbuilder.play;

using com.qifun.jsonStream.Plugins;
using com.thoughtworks.microbuilder.play.MyDeserializer;
using com.thoughtworks.restRpc.play.MySerializer;


@:build(com.qifun.jsonStream.rpc.OutgoingProxyFactory.generateOutgoingProxyFactory([
  "com.thoughtworks.microbuilder.play.MyRpc",
  "com.thoughtworks.microbuilder.play.MyRpcWithStructuralException"
]))
class MyOutgoingProxyFactory {}