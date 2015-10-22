package com.thoughtworks.restRpc.play;

using com.qifun.jsonStream.Plugins;

@:build(com.qifun.jsonStream.JsonSerializer.generateSerializer([
    "com.thoughtworks.restRpc.core.Failure",
    "com.thoughtworks.restRpc.play.MyModels"
]))
class MySerializer {

}