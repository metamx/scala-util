package com.metamx.common.scala

import com.metamx.common.scala.Predef._
import com.simple.simplespec.Matchers
import java.io.{ObjectInputStream, ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream}
import org.junit.{Ignore, Test}

@Ignore
class SerializableLogging(val n: Int) extends Logging with Serializable

class LoggingTest extends Matchers
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
