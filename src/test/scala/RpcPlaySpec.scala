import org.specs2.Specification

class RpcPlaySpec extends Specification {
  def is = s2"""

   This is a specification for the RpcPlay

   The RpcPlay should
     have identity startWith                            $e1
     have identity endWith                             $e2
                                                       """

  def e1 = RpcPlay.identity must startWith("the rest-rpc-play depends on")

  def e2 = RpcPlay.identity must endWith(RpcCore.identity)

}