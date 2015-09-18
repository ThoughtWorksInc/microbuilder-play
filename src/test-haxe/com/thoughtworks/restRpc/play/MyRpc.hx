package com.thoughtworks.restRpc.play;

import com.qifun.jsonStream.rpc.Future;
import com.thoughtworks.restRpc.play.MyModels.MyResponse;
import com.thoughtworks.restRpc.play.MyModels.Book;
import com.thoughtworks.restRpc.play.MyModels.CreatedResponse;


@:nativeGen
interface MyRpc {

    @:route("GET", "/my-method/{p1}/name/{name}")
    function myMethod(id:Int, name:String):Future<MyResponse>; // Future1   apply

    @:route("POST", "/{resourceName}")
    function createResource(resourceName: String, body:Book): Future<CreatedResponse>;
}