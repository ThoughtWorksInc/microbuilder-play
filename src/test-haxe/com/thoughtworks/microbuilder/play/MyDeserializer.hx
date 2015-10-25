package com.thoughtworks.microbuilder.play;

using jsonStream.Plugins;

@:build(jsonStream.JsonDeserializer.generateDeserializer([
    "com.thoughtworks.microbuilder.core.Failure",
    "com.thoughtworks.microbuilder.play.MyModels"
]))
class MyDeserializer {

}
