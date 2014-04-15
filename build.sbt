
// Keep in sync with the Maven pom.xml file!
// See http://www.scala-sbt.org/release/docs/Community/Using-Sonatype.html for how to publish to
// Sonatype, using sbt only.

name := "yaidom-xbrl"

organization := "eu.cdevreeze.yaidom"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.10.3"

crossScalaVersions := Seq("2.10.3")

scalacOptions ++= Seq("-unchecked", "-deprecation")

scalacOptions <++= scalaBinaryVersion map { version =>
  if (version.contains("2.10")) Seq("-feature") else Seq()
}

libraryDependencies += "eu.cdevreeze.yaidom" %% "yaidom" % "0.8.0"

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.0" % "test"

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { repo => false }

pomExtra := {
  <url>https://github.com/dvreeze/yaidom-xbrl</url>
  <licenses>
    <license>
      <name>Apache License, Version 2.0</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      <distribution>repo</distribution>
      <comments>Yaidom-xbrl is licensed under Apache License, Version 2.0</comments>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:git@github.com:dvreeze/yaidom-xbrl.git</connection>
    <url>https://github.com/dvreeze/yaidom-xbrl.git</url>
    <developerConnection>scm:git:git@github.com:dvreeze/yaidom-xbrl.git</developerConnection>
  </scm>
  <developers>
    <developer>
      <id>dvreeze</id>
      <name>Chris de Vreeze</name>
      <email>chris.de.vreeze@caiway.net</email>
    </developer>
  </developers>
}
