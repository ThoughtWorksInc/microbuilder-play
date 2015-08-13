package com.thoughtworks.restRpc.play;

using com.qifun.jsonStream.Plugins;
using com.thoughtworks.restRpc.play.MyDeserializer;


@:build(com.qifun.jsonStream.rpc.OutgoingProxyFactory.generateOutgoingProxyFactory([
  "com.thoughtworks.restRpc.play.MyRpc"
]))
class MyOutgoingProxyFactory {}