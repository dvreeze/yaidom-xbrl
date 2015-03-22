
// Keep in sync with the Maven pom.xml file!
// See http://www.scala-sbt.org/release/docs/Community/Using-Sonatype.html for how to publish to
// Sonatype, using sbt only.

name := "yaidom-xbrl"

organization := "eu.cdevreeze.yaidom"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.11.5"

crossScalaVersions := Seq("2.11.5")

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

libraryDependencies += "eu.cdevreeze.yaidom" %% "yaidom" % "1.3.6"

libraryDependencies += "eu.cdevreeze.yaidom" %% "yaidom-xlink" % "1.3.6"

libraryDependencies += "net.sf.saxon" % "Saxon-HE" % "9.5.1-8"

libraryDependencies += ("joda-time" % "joda-time" % "2.3").intransitive()

libraryDependencies += ("org.joda" % "joda-convert" % "1.2").intransitive()

libraryDependencies += "junit" % "junit" % "4.11" % "test"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.3" % "test"

// resolvers += "Artima Maven Repository" at "http://repo.artima.com/releases"

// addCompilerPlugin("com.artima.supersafe" %% "supersafe" % "1.0.3")

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
