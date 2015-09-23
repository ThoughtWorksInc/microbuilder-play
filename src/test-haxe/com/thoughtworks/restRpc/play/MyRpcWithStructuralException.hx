package com.thoughtworks.restRpc.play;

import com.qifun.jsonStream.rpc.Future;
import com.thoughtworks.restRpc.play.MyModels.MyResponse;
import com.thoughtworks.restRpc.play.MyModels.Book;
import com.thoughtworks.restRpc.play.MyModels.CreatedResponse;


@:nativeGen
@:structuralFailure("com.thoughtworks.restRpc.play.GeneralFailure")
interface MyRpcWithStructualException {
    @:route("GET", "/my-method/{p1}/name/{name}")
    function myMethod(id:Int, name:String):Future<MyResponse>;
}