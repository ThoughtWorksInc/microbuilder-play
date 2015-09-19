package com.thoughtworks.restRpc.play.exception

import com.thoughtworks.restRpc.core.Failure

case class RpcApplicationException(failure:Failure) extends RuntimeException{
}
