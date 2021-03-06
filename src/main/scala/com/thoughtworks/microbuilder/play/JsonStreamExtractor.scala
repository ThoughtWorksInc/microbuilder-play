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

import jsonStream.JsonStream
import jsonStream.JsonStreamPair

private[play] object JsonStreamExtractor {

  private val JsonStreamObjectIndex = {
    haxe.root.Type.getEnumConstructs(classOf[JsonStream]).indexOf("OBJECT", 0)
  }

  private val JsonStreamArrayIndex = {
    haxe.root.Type.getEnumConstructs(classOf[JsonStream]).indexOf("ARRAY", 0)
  }

  private val JsonStreamStringIndex = {
    haxe.root.Type.getEnumConstructs(classOf[JsonStream]).indexOf("STRING", 0)
  }

  private val JsonStreamInt32Index = {
    haxe.root.Type.getEnumConstructs(classOf[JsonStream]).indexOf("INT32", 0)
  }

  private val JsonStreamNumberIndex = {
    haxe.root.Type.getEnumConstructs(classOf[JsonStream]).indexOf("NUMBER", 0)
  }

  object Array {

    final def unapply(jsonStream: JsonStream): Option[WrappedHaxeIterator[JsonStream]] = {
      haxe.root.Type.enumIndex(jsonStream) match {
        case JsonStreamArrayIndex => {
          Some(WrappedHaxeIterator(haxe.root.Type.enumParameters(jsonStream).__a(0)).asInstanceOf[WrappedHaxeIterator[JsonStream]])
        }
        case _ => None
      }
    }

  }

  object Object {

    final def unapply(jsonStream: JsonStream): Option[WrappedHaxeIterator[JsonStreamPair]] = {
      haxe.root.Type.enumIndex(jsonStream) match {
        case JsonStreamObjectIndex => {
          Some(WrappedHaxeIterator(haxe.root.Type.enumParameters(jsonStream).__a(0)).asInstanceOf[WrappedHaxeIterator[JsonStreamPair]])
        }
        case _ => None
      }
    }

  }

  object Number {

    final def unapply(jsonStream: JsonStream): Option[Double] = {
      haxe.root.Type.enumIndex(jsonStream) match {
        case JsonStreamNumberIndex => {
          Some(haxe.root.Type.enumParameters(jsonStream).__a(0).asInstanceOf[Double])
        }
        case _ => None
      }
    }
  }

  object Int32 {

    final def unapply(jsonStream: JsonStream): Option[Int] = {
      haxe.root.Type.enumIndex(jsonStream) match {
        case JsonStreamInt32Index => {
          Some(haxe.root.Type.enumParameters(jsonStream).__a(0).asInstanceOf[Int])
        }
        case _ => None
      }
    }
  }

  object String {

    final def unapply(jsonStream: JsonStream): Option[String] = {
      haxe.root.Type.enumIndex(jsonStream) match {
        case JsonStreamStringIndex => {
          Some(haxe.root.Type.enumParameters(jsonStream).__a(0).asInstanceOf[String])
        }
        case _ => None
      }
    }
  }
}
