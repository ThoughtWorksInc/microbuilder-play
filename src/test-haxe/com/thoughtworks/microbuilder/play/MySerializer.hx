package com.thoughtworks.microbuilder.play;

using jsonStream.Plugins;

@:build(jsonStream.JsonSerializer.generateSerializer([
    "com.thoughtworks.microbuilder.core.Failure",
    "com.thoughtworks.microbuilder.play.MyModels"
]))
class MySerializer {

}