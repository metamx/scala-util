/*
 * Copyright 2012 Metamarkets Group Inc.
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

package com.metamx.common.scala

import com.metamx.common.scala.Predef._
import com.metamx.common.scala.untyped._
import com.simple.simplespec.Matchers
import java.{util => ju}
import org.junit.Test
import scala.collection.JavaConverters._

class UntypedTest extends Matchers {

  def jList[X]  (xs: X*)     = new ju.ArrayList[X] withEffect { _.asScala ++= xs }
  def jMap[K,V] (xs: (K,V)*) = new ju.HashMap[K,V] withEffect { _.asScala ++= xs }

  val obj = new { val x = 0 }

  // These need to be by-name (def) instead of by-value (val) because of Iterator, which isn't pure
  def messy = jList(jMap("a" -> Seq(Map("b" -> Iterable(Iterator(Array(1, "c", false, (2,true), obj)))))))
  def norm  = List(Map("a" -> List(Map("b" -> List(List(List(1, "c", false, (2,true), obj)))))))
  def jnorm = jList(jMap("a" -> jList(jMap("b" -> jList(jList(jList(1, "c", false, (2,true), obj)))))))

  @Test def normalizing {
    normalize(messy) must be(norm)
  }

  @Test def normalizingJava {
    normalizeJava(messy).toString must be(jnorm.toString)
  }

  @Test def normalizingRepeatedly {
    normalize(normalize(messy))                  must be(normalize(messy))
    normalize(normalizeJava(messy))              must be(normalize(messy))
    normalizeJava(normalize(messy)).toString     must be(normalizeJava(messy).toString)
    normalizeJava(normalizeJava(messy)).toString must be(normalizeJava(messy).toString)
  }

}
