/**
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

organization := "com.metamx"

name := "scala-util"

scalaVersion := "2.11.7"

crossScalaVersions := Seq("2.10.5", "2.11.7")

lazy val root = project.in(file("."))

net.virtualvoid.sbt.graph.Plugin.graphSettings

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/metamx/scala-util"))

publishMavenStyle := true

publishTo := Some("releases" at "https://oss.sonatype.org/service/local/staging/deploy/maven2/")

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>https://github.com/metamx/scala-util.git</url>
    <connection>scm:git:git@github.com:metamx/scala-util.git</connection>
  </scm>
  <developers>
    <developer>
      <name>Gian Merlino</name>
      <organization>Metamarkets Group Inc.</organization>
      <organizationUrl>https://www.metamarkets.com</organizationUrl>
    </developer>
  </developers>)

parallelExecution in Test := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-Duser.timezone=UTC")

releaseSettings

ReleaseKeys.publishArtifactsAction := PgpKeys.publishSigned.value

val jacksonFasterxmlVersion = "2.6.0"
val curatorVersion = "2.6.0"
val zookeeperVersion = "3.4.5"
val twittersVersion = "6.25.0"

libraryDependencies ++= Seq(
  "com.metamx" %% "loglady" % "1.1.0-mmx" force()
)

libraryDependencies ++= Seq(
  "com.metamx" % "java-util" % "0.27.4" force(),
  "com.metamx" % "http-client" % "1.0.3" force(),
  "com.metamx" % "emitter" % "0.3.3" force(),
  "com.metamx" % "server-metrics" % "0.2.8" force()
)

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.2" force(),
  "commons-lang" % "commons-lang" % "2.6" force(),
  "joda-time" % "joda-time" % "2.1" force(),
  "org.joda" % "joda-convert" % "1.6" force(),
  "org.scalaj" %% "scalaj-time" % "0.5" force(),
  "org.skife.config" % "config-magic" % "0.9" force(),
  "com.google.guava" % "guava" % "16.0.1" force(),
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
  "org.jdbi" % "jdbi" % "2.70" force(),
  "mysql" % "mysql-connector-java" % "5.1.18" force(),
  "com.h2database" % "h2" % "1.3.158" force(),
  "c3p0" % "c3p0" % "0.9.1.2" force()
)

libraryDependencies ++= Seq(
  "org.apache.zookeeper" % "zookeeper" % zookeeperVersion exclude("log4j", "log4j") exclude("org.slf4j", "slf4j-log4j12") exclude("org.jboss.netty", "netty") force(),
  "org.apache.curator" % "curator-framework" % curatorVersion exclude("org.jboss.netty", "netty") force(),
  "org.apache.curator" % "curator-recipes" % curatorVersion exclude("org.jboss.netty", "netty") force(),
  "org.apache.curator" % "curator-x-discovery" % curatorVersion exclude("org.jboss.netty", "netty") force()
)

libraryDependencies ++= Seq(
  "com.twitter" %% "util-core" % twittersVersion force(),
  "com.twitter" %% "finagle-core" % twittersVersion force(),
  "com.twitter" %% "finagle-http" % twittersVersion force()
)

libraryDependencies ++= Seq(
  "io.netty" % "netty" % "3.10.4.Final" force()
)

//
// Test stuff.
//

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.2" % "test" force(),
  "junit" % "junit" % "4.11" % "test" force(),
  "org.mockito" % "mockito-core" % "1.9.5" % "test" force()
)

libraryDependencies <++= scalaVersion {
  case x if x.startsWith("2.10.") => Seq(
    "com.simple" % "simplespec_2.10.2" % "0.8.4" % "test" exclude("org.mockito", "mockito-all") force()
  )
  case _ => Seq(
    "com.simple" %% "simplespec" % "0.8.4" % "test" exclude("org.mockito", "mockito-all") force()
  )
}

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11-RC1" % "test" exclude("junit", "junit") force()
)
