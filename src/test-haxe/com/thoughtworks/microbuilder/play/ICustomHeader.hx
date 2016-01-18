package com.thoughtworks.microbuilder.play;

import jsonStream.rpc.Future;
import com.thoughtworks.microbuilder.play.MyModels.MyResponse;
import com.thoughtworks.microbuilder.play.MyModels.Book;
import com.thoughtworks.microbuilder.play.MyModels.CreatedResponse;

@:nativeGen
interface ICustomHeader {

    @:route("GET", "my-method/{id}/name/{name}")
    @:requestHeader("My-Custom-Static-Header", "my-value")
    @:requestHeader("My-Custom-Dynamic-Header-0", id)
    @:requestHeader("My-Custom-Dynamic-Header-1", name)
    @:requestHeader("My-Custom-Dynamic-Header-2", myHeaderValue2)
    function myMethod(id:Int, name:String, myHeaderValue2:String):Future<MyResponse>;

}
