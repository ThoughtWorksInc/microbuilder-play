package com.thoughtworks.microbuilder.play

import jsonStream.JsonStream
import org.specs2.mutable.Specification

class RouteSpec extends Specification {

  "MyRouteConfigurationFactory should" >> {
    "able to format URL" >> {
      val routeConfiguration = MyRouteConfigurationFactory.routeConfiguration_com_thoughtworks_microbuilder_play_MyRpc
      val template = routeConfiguration.nameToUriTemplate("myMethod")
       template.render(Iterator(JsonStream.NUMBER(1.0), JsonStream.STRING("xx")) ) must equalTo("/my-method/1/name/xx")
    }
  }

}
