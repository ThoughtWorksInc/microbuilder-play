package com.thoughtworks.microbuilder.play;

using com.qifun.jsonStream.Plugins;

@:build(com.qifun.jsonStream.JsonDeserializer.generateDeserializer([
    "com.thoughtworks.microbuilder.core.Failure",
    "com.thoughtworks.microbuilder.play.MyModels"
]))
class MyDeserializer {

}
