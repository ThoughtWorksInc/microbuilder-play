package com.thoughtworks.microbuilder.play

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

/**
  * @author 杨博 (Yang Bo) &lt;pop.atry@gmail.com&gt;
  */
@RunWith(classOf[JUnitRunner])
class OutgoingStubSpec extends Specification {
  "OutgoingStub should be generated for MyRpc" >> {
    import MyRouteConfigurationFactory._
    import MyOutgoingProxyFactory._
    implicitly[OutgoingStub[MyRpc]] must not(equalTo(null))
  }
}
