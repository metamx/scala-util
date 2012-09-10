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

// Implicits for everything in scala.math that isn't already in runtime.Rich*

object Math {

  class RichInt(a: Int) {

    def signum = math signum a

    def pow (b: Int)    = math pow (a,b) toInt
    def pow (b: Long)   = math pow (a,b) toLong
    def pow (b: Float)  = math pow (a,b) toFloat
    def pow (b: Double) = math pow (a,b) toDouble
    def **  (b: Int)    = math pow (a,b) toInt
    def **  (b: Long)   = math pow (a,b) toLong
    def **  (b: Float)  = math pow (a,b) toFloat
    def **  (b: Double) = math pow (a,b) toDouble

  }

  class RichLong(a: Long) {

    def signum = math signum a

    def pow (b: Long)   = math pow (a,b) toLong
    def pow (b: Float)  = math pow (a,b) toFloat
    def pow (b: Double) = math pow (a,b) toDouble
    def **  (b: Long)   = math pow (a,b) toLong
    def **  (b: Float)  = math pow (a,b) toFloat
    def **  (b: Double) = math pow (a,b) toDouble

  }

  class RichFloat(a: Float) {

    def signum = math signum a
    def ulp    = math ulp    a

    def pow (b: Float)  = math pow (a,b) toFloat
    def pow (b: Double) = math pow (a,b) toDouble
    def **  (b: Float)  = math pow (a,b) toFloat
    def **  (b: Double) = math pow (a,b) toDouble

  }

  class RichDouble(a: Double) {

    def sin    = math sin    a
    def cos    = math cos    a
    def tan    = math tan    a
    def asin   = math asin   a
    def acos   = math acos   a
    def atan   = math atan   a
    def exp    = math exp    a
    def log    = math log    a
    def sqrt   = math sqrt   a
    def rint   = math rint   a
    def signum = math signum a
    def log10  = math log10  a
    def cbrt   = math cbrt   a
    def ulp    = math ulp    a
    def sinh   = math sinh   a
    def cosh   = math cosh   a
    def tanh   = math tanh   a
    def expm1  = math expm1  a
    def log1p  = math log1p  a

    def IEEEremainder (b: Double) = math IEEEremainder (a,b)
    def atan2         (b: Double) = math atan2         (a,b)
    def pow           (b: Double) = math pow           (a,b)
    def **            (b: Double) = math pow           (a,b)
    def hypot         (b: Double) = math hypot         (a,b)

  }

  implicit def RichInt    (a: Int)    = new RichInt    (a)
  implicit def RichLong   (a: Long)   = new RichLong   (a)
  implicit def RichFloat  (a: Float)  = new RichFloat  (a)
  implicit def RichDouble (a: Double) = new RichDouble (a)

}
