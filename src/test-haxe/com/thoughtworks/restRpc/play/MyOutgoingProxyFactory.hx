package com.thoughtworks.restRpc.play;

using com.qifun.jsonStream.Plugins;
using com.thoughtworks.restRpc.play.MyDeserializer;
using com.thoughtworks.restRpc.play.MySerializer;


@:build(com.qifun.jsonStream.rpc.OutgoingProxyFactory.generateOutgoingProxyFactory([
  "com.thoughtworks.restRpc.play.MyRpc",
  "com.thoughtworks.restRpc.play.MyRpcWithStructuralException"
]))
class MyOutgoingProxyFactory {}