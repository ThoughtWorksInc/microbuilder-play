/*
 * microbuilder-play
 * Copyright 2015 ThoughtWorks, Inc. & 深圳岂凡网络有限公司 (Shenzhen QiFun Network Corp., LTD)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thoughtworks.microbuilder.play

import com.dongxiguo.continuation.utils.{ Generator => HaxeGenerator }
import haxe.root.Reflect

private[play] trait WrappedHaxeIterator[+Element] extends Iterator[Element] {
  def haxeIterator: AnyRef
}

private[play] object WrappedHaxeIterator {

  private final class WrappedHaxeGenerator[+Element](haxeGenerator: HaxeGenerator[_ <: Element]) extends WrappedHaxeIterator[Element] {

    override final def hasNext = haxeGenerator.hasNext

    override final def next() = haxeGenerator.next().asInstanceOf[Element]

    override final def haxeIterator = haxeGenerator

  }

  private final class WrappedReflectiveIterator(override val haxeIterator: AnyRef) extends WrappedHaxeIterator[Any] {

    override final def hasNext = {
      Reflect.callMethod(haxeIterator, Reflect.field(haxeIterator, "hasNext"), new haxe.root.Array()).asInstanceOf[Boolean]
    }

    override final def next() = {
      Reflect.callMethod(haxeIterator, Reflect.field(haxeIterator, "next"), new haxe.root.Array())
    }

  }

  final def apply(haxeIterator: AnyRef): WrappedHaxeIterator[Any] = {
    haxeIterator match {
      case generator: HaxeGenerator[_] => new WrappedHaxeGenerator(generator)
      case _ => new WrappedReflectiveIterator(haxeIterator)
    }
  }

}