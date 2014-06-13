organization := "com.metamx"

name := "scala-util"

scalaVersion := "2.9.1"

crossScalaVersions := Seq("2.9.1", "2.10.4")

lazy val root = project.in(file("."))

net.virtualvoid.sbt.graph.Plugin.graphSettings

resolvers ++= Seq(
  "Metamarkets Releases" at "https://metamx.artifactoryonline.com/metamx/libs-releases/"
)

publishMavenStyle := true

publishTo := Some("pub-libs" at "https://metamx.artifactoryonline.com/metamx/pub-libs-releases-local")

parallelExecution in Test := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-Duser.timezone=UTC")

releaseSettings

// When updating Jackson, watch out for: https://github.com/FasterXML/jackson-module-scala/issues/148
val jacksonFasterxmlVersion = "2.2.2"
val curatorVersion = "2.4.0"
val zookeeperVersion = "3.4.5"
val twittersVersion = "6.16.0"
val simplespecVersion = "0.7.0"

libraryDependencies ++= Seq(
  "org.eintr.loglady" %% "loglady" % "1.1.0" force()
)

libraryDependencies ++= Seq(
  "com.metamx" % "java-util" % "0.25.1" force(),
  "com.metamx" % "http-client" % "0.9.6" force(),
  "com.metamx" % "emitter" % "0.2.12" force(),
  "com.metamx" % "server-metrics" % "0.0.9" force()
)

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.2" force(),
  "org.slf4j" % "slf4j-log4j12" % "1.7.2" force(),
  "log4j" % "log4j" % "1.2.17" force(),
  "commons-lang" % "commons-lang" % "2.6" force(),
  "joda-time" % "joda-time" % "2.1" force(),
  "org.joda" % "joda-convert" % "1.6" force(),
  "org.scala-tools.time" %% "time" % "0.6-mmx1" force(),
  "org.skife.config" % "config-magic" % "0.9" force(),
  "com.google.guava" % "guava" % "15.0" force(),
  "org.yaml" % "snakeyaml" % "1.11" force()
)

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonFasterxmlVersion force(),
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonFasterxmlVersion force(),
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonFasterxmlVersion force(),
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-smile" % jacksonFasterxmlVersion force(),
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonFasterxmlVersion force(),
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonFasterxmlVersion force()
)

libraryDependencies ++= Seq(
  "org.jdbi" % "jdbi" % "2.27" force(),
  "mysql" % "mysql-connector-java" % "5.1.18" force(),
  "com.h2database" % "h2" % "1.3.158" force(),
  "c3p0" % "c3p0" % "0.9.1.2" force()
)

libraryDependencies ++= Seq(
  "org.apache.zookeeper" % "zookeeper" % zookeeperVersion exclude("log4j", "log4j") exclude("org.slf4j", "slf4j-log4j12") force(),
  "org.apache.curator" % "curator-framework" % curatorVersion exclude("org.jboss.netty", "netty") force(),
  "org.apache.curator" % "curator-recipes" % curatorVersion exclude("org.jboss.netty", "netty") force(),
  "org.apache.curator" % "curator-x-discovery" % curatorVersion exclude("org.jboss.netty", "netty") force()
)

def TwitterCross = CrossVersion.binaryMapped {
  case "2.9.1" => "2.9.2"
  case x => x
}

libraryDependencies ++= Seq(
  "com.twitter" % "util-core" % twittersVersion cross TwitterCross force(),
  "com.twitter" % "finagle-core" % twittersVersion cross TwitterCross force(),
  "com.twitter" % "finagle-http" % twittersVersion cross TwitterCross force()
)

libraryDependencies ++= Seq(
  "io.netty" % "netty" % "3.9.0.Final" force()
)

//
// Test stuff.
//

libraryDependencies <++= scalaVersion {
  case "2.9.1" => Seq(
    "junit" % "junit" % "4.10" % "test" force(),
    "com.simple" % "simplespec_2.9.2" % "0.7.0" % "test"
  )
  case "2.10.4" => Seq(
    "junit" % "junit" % "4.11" % "test" force(),
    "com.simple" % "simplespec_2.10.2" % "0.8.4" % "test" exclude("org.mockito", "mockito-all") force(),
    "org.mockito" % "mockito-core" % "1.9.5" % "test" force()
  )
}

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11-RC1" % "test" exclude("junit", "junit") force()
)
