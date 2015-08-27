package com.thoughtworks.restRpc.play;

import com.qifun.jsonStream.rpc.Future;
import com.thoughtworks.restRpc.play.MyModels.MyResponse;
import com.thoughtworks.restRpc.play.MyModels.Book;


@:nativeGen
interface MyRpc {

    @:route("GET", "/my-method/{p1}/name/{name}")
    function myMethod(id:Int, name:String):Future<MyResponse>; // Future1   apply

    @:route("POST", "/{resourceName}")
    function createResource(resourceName: String, body:Book): Future<String>;
//
//    @:route("GET", "/my-method/{p1}?version=v2{&p2}")
//    function myMethod(p1:Int, p2:Null<String>):Future<Void>; // Future0   apply
//
//    @:route("GET", "/my-method/{p1}?version=v2{&p2}")
//    function myMethod(p1:Int, p2:Null<String>):Void; // Unit   push

}