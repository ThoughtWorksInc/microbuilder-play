package com.thoughtworks.restRpc.callee

import com.qifun.jsonStream._RawJson.{RawJson_Impl_ => RawJson}
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._

@RunWith(classOf[JUnitRunner])
class MainControllerSpec extends Specification {


    "call add(1,2, 3) === 6" in new WithApplication() {

//      val map: HashMap[Class, Class] = new HashMap[Class, Class]()
//      val controller = new MainController(map)


//      val body = new RawJson("""{"method": "add", "params": "[1, 2, 3]"}""")

//      var rawData = JsonSerializer.serializeRaw(body)

//      val Some(result) = controller.call(rawData)

      val result = route(FakeRequest(GET, "/rpc/method/name/1/2")).get

//      val content = contentAsJson(result)

//      val resultJson = JsonDeserializer.deserializeRaw(result)

      result must equalTo("""{"result": "6"}""")
    }
}
