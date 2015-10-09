package com.thoughtworks.restRpc.play;

using com.qifun.jsonStream.Plugins;

@:build(com.qifun.jsonStream.JsonDeserializer.generateDeserializer([
    "com.thoughtworks.restRpc.core.Failure",
    "com.thoughtworks.restRpc.play.MyModels"
]))
class MyDeserializer {

}