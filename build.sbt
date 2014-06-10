organization := "com.metamx"

name := "scala-util"

scalaVersion := "2.9.1"

crossScalaVersions := Seq("2.9.1", "2.10.4")

lazy val root = project.in(file("."))

net.virtualvoid.sbt.graph.Plugin.graphSettings

resolvers ++= Seq(
  "Metamarkets Releases" at "https://metamx.artifactoryonline.com/metamx/pub-libs-releases-local/"
)

publishMavenStyle := true

publishTo := Some("pub-libs" at "https://metamx.artifactoryonline.com/metamx/pub-libs-releases-local")

parallelExecution in Test := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-Duser.timezone=UTC")

releaseSettings

// When updating Jackson, watch out for: https://github.com/FasterXML/jackson-module-scala/issues/148
val jacksonFasterxmlVersion = "2.2.2"
val curatorVersion = "2.3.0"
val twittersVersion = "6.16.0"
val simplespecVersion = "0.7.0"

libraryDependencies ++= Seq(
  "org.eintr.loglady" %% "loglady" % "1.1.0"
)

libraryDependencies ++= Seq(
  "com.metamx" % "java-util" % "0.25.1",
  "com.metamx" % "http-client" % "0.8.2" exclude("org.jboss.netty", "netty"),
  "com.metamx" % "emitter" % "0.2.4",
  "com.metamx" % "server-metrics" % "0.0.4"
)

libraryDependencies ++= Seq(
  "commons-lang" % "commons-lang" % "2.6",
  "joda-time" % "joda-time" % "1.6",
  "org.joda" % "joda-convert" % "1.6",
  "org.scala-tools.time" % "time_2.8.1" % "0.6.mmx0",
  "org.skife.config" % "config-magic" % "0.9",
  "com.google.guava" % "guava" % "14.0.1",
  "org.yaml" % "snakeyaml" % "1.9"
)

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonFasterxmlVersion,
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonFasterxmlVersion,
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonFasterxmlVersion,
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-smile" % jacksonFasterxmlVersion,
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonFasterxmlVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonFasterxmlVersion
)

libraryDependencies ++= Seq(
  "org.jdbi" % "jdbi" % "2.27",
  "mysql" % "mysql-connector-java" % "5.1.18",
  "com.h2database" % "h2" % "1.3.158",
  "c3p0" % "c3p0" % "0.9.1.2"
)

libraryDependencies ++= Seq(
  "org.apache.curator" % "curator-framework" % curatorVersion exclude("org.jboss.netty", "netty"),
  "org.apache.curator" % "curator-recipes" % curatorVersion exclude("org.jboss.netty", "netty"),
  "org.apache.curator" % "curator-x-discovery" % curatorVersion exclude("org.jboss.netty", "netty")
)

def TwitterCross = CrossVersion.binaryMapped {
  case "2.9.1" => "2.9.2"
  case x => x
}

libraryDependencies ++= Seq(
  "com.twitter" % "util-core" % twittersVersion cross TwitterCross,
  "com.twitter" % "finagle-core" % twittersVersion cross TwitterCross,
  "com.twitter" % "finagle-http" % twittersVersion cross TwitterCross
)

libraryDependencies ++= Seq(
  "io.netty" % "netty" % "3.8.0.Final"
)

libraryDependencies <+= scalaVersion {
  case "2.9.1" => "com.simple" % "simplespec_2.9.2" % "0.7.0" % "test"
  case "2.10.4" => "com.simple" % "simplespec_2.10.2" % "0.8.4" % "test"
}

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.10" % "test"
)
