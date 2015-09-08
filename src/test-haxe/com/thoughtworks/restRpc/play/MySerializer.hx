package com.thoughtworks.restRpc.play;

using com.qifun.jsonStream.Plugins;

@:build(com.qifun.jsonStream.JsonSerializer.generateSerializer([
    "com.thoughtworks.restRpc.play.MyModels"
]))
class MySerializer {

}