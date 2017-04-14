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

lazy val root = project.in(file("."))

organization := "com.metamx"

name := "scala-util"

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.10.6", "2.11.8", "2.12.1")

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

homepage := Some(url("https://github.com/metamx/scala-util"))

resolvers += "sigar" at "https://repository.jboss.org/nexus/content/repositories/thirdparty-uploads/"

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
      <name>Metamarkets Open Source Team</name>
      <email>oss@metamarkets.com</email>
      <organization>Metamarkets Group Inc.</organization>
      <organizationUrl>https://www.metamarkets.com</organizationUrl>
    </developer>
  </developers>)

parallelExecution in Test := false

testOptions += Tests.Argument(TestFrameworks.JUnit, "-Duser.timezone=UTC")

releasePublishArtifactsAction := PgpKeys.publishSigned.value

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalacOptions ++= Seq("-unchecked", "-deprecation")

scalacOptions ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 12)) => Seq()
    case _ => Seq("-target:jvm-1.7")
  }
}

val curatorVersion = "2.11.1"
val zookeeperVersion = "3.4.10"

lazy val jacksonFasterxmlVersion = settingKey[String]("Jackson version")
jacksonFasterxmlVersion := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 10 | 11)) => "2.6.7"
    case _ => "2.8.7"
  }
}

lazy val twitterUtilsVersion = settingKey[String]("Twitter utils version")
twitterUtilsVersion := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 10)) => "6.34.0"
    case _ => "6.41.0"
  }
}

lazy val twittersVersion = settingKey[String]("Twitters version")
twittersVersion := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 10)) => "6.35.0"
    case _ => "6.42.0"
  }
}

libraryDependencies ++= Seq(
  "com.metamx" % "java-util" % "0.28.2" force(),
  "com.metamx" % "http-client" % "1.1.0" force(),
  "com.metamx" % "emitter" % "0.4.4" force(),
  "com.metamx" % "server-metrics" % "0.4.1" force()
)

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.25" force(),
  "joda-time" % "joda-time" % "2.9.7" force(),
  "org.joda" % "joda-convert" % "1.8.1" force(),
  "org.skife.config" % "config-magic" % "0.17" force(),
  "com.google.guava" % "guava" % "16.0.1" force(),
  "org.yaml" % "snakeyaml" % "1.11" force(),
  "com.github.nscala-time" %% "nscala-time" % "2.16.0" force()
)

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonFasterxmlVersion.value force(),
  "com.fasterxml.jackson.core" % "jackson-annotations" % jacksonFasterxmlVersion.value force(),
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonFasterxmlVersion.value force(),
  "com.fasterxml.jackson.dataformat" % "jackson-dataformat-smile" % jacksonFasterxmlVersion.value force(),
  "com.fasterxml.jackson.datatype" % "jackson-datatype-joda" % jacksonFasterxmlVersion.value force(),
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonFasterxmlVersion.value force()
)

libraryDependencies ++= Seq(
  "org.jdbi" % "jdbi" % "2.70" force(),
  "mysql" % "mysql-connector-java" % "5.1.18" force(),
  "com.h2database" % "h2" % "1.3.158" force(),
  "c3p0" % "c3p0" % "0.9.1.2" force()
)

libraryDependencies ++= Seq(
  "org.apache.zookeeper" % "zookeeper" % zookeeperVersion
    exclude("log4j", "log4j")
    exclude("org.slf4j", "slf4j-api")
    exclude("org.slf4j", "slf4j-log4j12")
    exclude("io.netty", "netty")
    exclude("org.jboss.netty", "netty")
    force(),
  "org.apache.curator" % "curator-framework" % curatorVersion exclude("org.jboss.netty", "netty") force(),
  "org.apache.curator" % "curator-recipes" % curatorVersion exclude("org.jboss.netty", "netty") force(),
  "org.apache.curator" % "curator-x-discovery" % curatorVersion exclude("org.jboss.netty", "netty") force()
)

libraryDependencies ++= Seq(
  "com.twitter" %% "util-core" % twitterUtilsVersion.value force(),
  "com.twitter" %% "finagle-core" % twittersVersion.value force(),
  "com.twitter" %% "finagle-http" % twittersVersion.value force()
)

libraryDependencies ++= Seq(
  "io.netty" % "netty" % "3.10.6.Final" force()
)

//
// Test stuff.
//

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-simple" % "1.7.25" % "test" force(),
  "junit" % "junit" % "4.11" % "test" force(),
  "org.mockito" % "mockito-core" % "1.9.5" % "test" force()
)

libraryDependencies ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 10)) => Seq(
      "com.simple" % "simplespec_2.10.2" % "0.8.4" % "test" exclude("org.mockito", "mockito-all") force()
    )
    case Some((2, 11)) => Seq(
      "com.simple" %% "simplespec" % "0.8.4" % "test" exclude("org.mockito", "mockito-all") force()
    )
    case _ => Seq(
      "com.simple" %% "simplespec" % "0.9.0" % "test" exclude("org.mockito", "mockito-all") force()
    )
  }
}

libraryDependencies ++= Seq(
  "com.novocode" % "junit-interface" % "0.11-RC1" % "test" exclude("junit", "junit") force()
)
