package com.thoughtworks.microbuilder.play;

using com.qifun.jsonStream.Plugins;

@:build(com.qifun.jsonStream.JsonSerializer.generateSerializer([
    "com.thoughtworks.microbuilder.core.Failure",
    "com.thoughtworks.microbuilder.play.MyModels"
]))
class MySerializer {

}