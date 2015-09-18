package com.thoughtworks.restRpc.play;

@:nativeGen
@:final
class MyResponse {
  public function new() {}

  public var myInnerEntity:MyInnerEntity;
}

@:nativeGen
@:final
@:strictJsonType
class MyInnerEntity {
  public function new() {}
  var code: Int;
  var message: String;
}

@:nativeGen
@:final
class Book {
    public function new(id:Int, name:String) {
        this.id = id;
        this.name = name;
    }
    public var id: Int;
    public var name: String;
}

@:nativeGen
@:final
class CreatedResponse {
    public function new() {}
    var result:String;
}