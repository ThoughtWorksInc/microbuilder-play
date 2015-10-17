package com.thoughtworks.microbuilder.play;

import com.qifun.jsonStream.rpc.Future;
import com.thoughtworks.microbuilder.play.MyModels.MyResponse;
import com.thoughtworks.microbuilder.play.MyModels.Book;
import com.thoughtworks.microbuilder.play.MyModels.CreatedResponse;


@:nativeGen
@:structuralFailure(com.thoughtworks.microbuilder.play.MyModels.GeneralFailure)
interface MyRpcWithStructuralException {
    @:route("GET", "/my-method/{id}/name/{name}")
    function myMethod(id:Int, name:String):Future<MyResponse>;
}