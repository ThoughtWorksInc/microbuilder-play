package controllers

import com.thoughtworks.restRpc.core.IRouteConfiguration
import play.api.mvc._

case class RpcEntry[RpcInterface](routeConfiguration:IRouteConfiguration, incomingService:IIncomingService, implementation:RpcInterface)

case class IIncomingService{
}

class MainController(rpcImplementations:Seq[RpcEntry[_]]) extends Controller{
  def rpc = Action {
    Ok("LLL")
  }
}
