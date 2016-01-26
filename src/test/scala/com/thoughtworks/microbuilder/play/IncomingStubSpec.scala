package com.thoughtworks.microbuilder.play

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
@RunWith(classOf[JUnitRunner])
class IncomingStubSpec extends Specification {
  "IncomingStub should be generated for MyRpc" >> {
    import MyRouteConfigurationFactory._
    import MyIncomingProxyFactory._
    implicitly[IncomingStub[MyRpc]] must not(equalTo(null))
  }
}
