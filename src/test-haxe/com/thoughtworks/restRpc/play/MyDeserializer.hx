package com.thoughtworks.restRpc.play;

using com.qifun.jsonStream.Plugins;

@:build(com.qifun.jsonStream.JsonDeserializer.generateDeserializer([
  "com.thoughtworks.restRpc.play.MyModels"
]))
class MyDeserializer {

}

@:build(com.qifun.jsonStream.JsonSerializer.generateSerializer([
    "com.thoughtworks.restRpc.play.MyModels"
]))
class MySerializer {

}