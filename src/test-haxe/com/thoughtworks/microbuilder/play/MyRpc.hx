package com.thoughtworks.microbuilder.play;

import jsonStream.rpc.Future;
import com.thoughtworks.microbuilder.play.MyModels.MyResponse;
import com.thoughtworks.microbuilder.play.MyModels.Book;
import com.thoughtworks.microbuilder.play.MyModels.CreatedResponse;

@:nativeGen
interface MyRpc {

    @:route("GET", "/my-method/{id}/name/{name}")
    function myMethod(id:Int, name:String):Future<MyResponse>; // Future1   apply

    @:route("POST", "/{resourceName}")
    function createResource(resourceName: String, body:Book): Future<CreatedResponse>;

    // TODO: Add methods that has a @:requestContentType

}