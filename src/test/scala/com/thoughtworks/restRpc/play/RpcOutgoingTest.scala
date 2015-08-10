package com.thoughtworks.restRpc.play

import com.github.dreamhead.moco._
import com.thoughtworks.restRpc.core.RouteConfiguration
import org.mockito.Mockito
import org.specs2.Specification
import org.specs2.mock.{Mockito => SpecMockito}

class RpcOutgoingTest extends Specification with SpecMockito {def is = s2"""

      This is a specification of using rest-rpc-play tools to make http requests

      Should be able to send a get request without parameters     $e1
                                                                  """

  def e1 = {
    val monitor = Mockito.mock(classOf[MocoMonitor])

    val theServer = Runner.runner(Moco.httpServer(8080, monitor))
    theServer.start()

    val myRpc:MyRpc = MyOutgoingProxyFactory.outgoingProxy_com_thoughtworks_restRpc_play_MyRpc(
      new PlayOutgoingJsonService("http://localhost:8080/", new RouteConfiguration())
    )

    myRpc.myMethod(1, "abc")
    theServer.stop()

    there was one(monitor).onMessageArrived(any)
  }
}
