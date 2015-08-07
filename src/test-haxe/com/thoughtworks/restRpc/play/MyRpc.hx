package com.thoughtworks.restRpc.play;

import com.qifun.jsonStream.rpc.Future;
import com.thoughtworks.restRpc.play.MyModels.MyResponse;


@:nativeGen
interface MyRpc {

  @:route("GET", "/my-method/{p1}?version=v2{&p2}")
  function myMethod(p1:Int, p2:Null<String>):Future<MyResponse>;

}