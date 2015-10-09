package com.thoughtworks.restRpc.play;

using com.qifun.jsonStream.Plugins;
using com.thoughtworks.restRpc.play.MySerializer;
using com.thoughtworks.restRpc.play.MyDeserializer;


@:build(com.qifun.jsonStream.rpc.IncomingProxyFactory.generateIncomingProxyFactory([
  "com.thoughtworks.restRpc.play.MyRpc"
]))
class MyIncomingProxyFactory {}