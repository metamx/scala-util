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

import com.codahale.simplespec.Spec
import com.metamx.common.scala.Predef._
import org.junit.Test

class prettySpec extends Spec {
  class A {

    @Test def parseBytes {

      pretty.parseBytes("0")   must be(0)
      pretty.parseBytes("0")   must be(0)
      pretty.parseBytes("0KB") must be(0)
      pretty.parseBytes("0MB") must be(0)
      pretty.parseBytes("0PB") must be(0)

      pretty.parseBytes("5")   must be(5L)
      pretty.parseBytes("5KB") must be(5L*1024)
      pretty.parseBytes("5MB") must be(5L*1024*1024)
      pretty.parseBytes("5GB") must be(5L*1024*1024*1024)

      pretty.parseBytes("1038524239")    must be(1038524239L)
      pretty.parseBytes("1038524239 KB") must be(1038524239L*1024)

      pretty.parseBytes("5 GB")       must be(5L*1024*1024*1024)
      pretty.parseBytes(" \t5\tGB  ") must be(5L*1024*1024*1024)

      evaluating { pretty.parseBytes("")     } must throwAn[IllegalArgumentException]
      evaluating { pretty.parseBytes("\t")   } must throwAn[IllegalArgumentException]
      evaluating { pretty.parseBytes("3.2")  } must throwAn[IllegalArgumentException]
      evaluating { pretty.parseBytes("5G")   } must throwAn[IllegalArgumentException]
      evaluating { pretty.parseBytes("5MBB") } must throwAn[IllegalArgumentException]
      evaluating { pretty.parseBytes("foo")  } must throwAn[IllegalArgumentException]

      evaluating { pretty.parseBytes("5BB")  } must not(throwAn[IllegalArgumentException])
      evaluating { pretty.parseBytes("5XB")  } must throwAn[IllegalArgumentException]

    }

  }
}
