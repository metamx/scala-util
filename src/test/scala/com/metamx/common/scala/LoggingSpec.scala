package com.metamx.common.scala

import org.junit.{Ignore, Test}
import com.simple.simplespec.Spec
import java.io.{ObjectInputStream, ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream}
import com.metamx.common.scala.Predef._

@Ignore
class SerializableLogging(val n: Int) extends Logging with Serializable

class LoggingSpec extends Spec
{

  class A
  {
    @Test
    def testJavaSerialization()
    {
      val x = new SerializableLogging(1)
      x.log.trace("Hello!")

      val xBytes = (new ByteArrayOutputStream() withEffect {
        baos =>
          new ObjectOutputStream(baos).writeObject(x)
      }).toByteArray

      val y = new ObjectInputStream(new ByteArrayInputStream(xBytes)).readObject().asInstanceOf[SerializableLogging]
      y.log.trace("World!")

      x.n must be(y.n)
      x eq y must be(false)
    }
  }

}
