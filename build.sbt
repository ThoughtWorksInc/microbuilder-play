enablePlugins(HaxeJavaPlugin)

organization := "com.thoughtworks.microbuilder"

name := "microbuilder-play"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.6.4" % Test

libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.4.2"

libraryDependencies += "com.thoughtworks.microbuilder" %% "microbuilder-core" % "0.1.0" % TestHaxeJava classifier "haxe-java"

libraryDependencies += "com.thoughtworks.microbuilder" %% "microbuilder-core" % "0.1.0"

libraryDependencies += "com.thoughtworks.microbuilder" %% "json-stream" % "2.0.3" % TestHaxeJava classifier "haxe-java"

libraryDependencies += "com.thoughtworks.microbuilder" %% "json-stream" % "2.0.3" % Provided

libraryDependencies += "com.qifun" %% "haxe-scala-stm" % "0.1.4" % TestHaxeJava classifier "haxe-java"

libraryDependencies += "org.scala-stm" %% "scala-stm" % "0.7" % Test

libraryDependencies += "org.specs2" %% "specs2-mock" % "3.6.4" % Test

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % Test

libraryDependencies += "com.github.dreamhead" % "moco-core" % "0.10.1" % Test

libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.4.0" % Test

libraryDependencies += "com.typesafe.play" %% "play-specs2" % "2.4.2" % Test

val haxelibs = Map(
  "microbuilder-core" -> DependencyVersion.SpecificVersion("0.1.0")
)

haxelibDependencies ++= haxelibs

for (c <- Seq(Compile, Test)) yield {
  haxeOptions in c ++= haxelibOptions(haxelibs)
}

for (c <- Seq(Compile, Test)) yield {
  haxeOptions in c ++= Seq("-D", "scala")
}

for (c <- Seq(Compile, Test)) yield {
  haxeOptions in c ++= Seq("-dce", "no")
}

scalacOptions in Test += "-Yrangepos"

compileOrder := CompileOrder.JavaThenScala

crossScalaVersions := Seq("2.10.6", "2.11.7")

developers := List(
  Developer(
    "Atry",
    "杨博 (Yang Bo)",
    "pop.atry@gmail.com",
    url("https://github.com/Atry")
  )
)


homepage := Some(url(s"https://github.com/ThoughtWorksInc/${name.value}"))

startYear := Some(2015)

releasePublishArtifactsAction := PgpKeys.publishSigned.value

import ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeRelease"),
  pushChanges
)

releaseUseGlobalVersion := false

releaseCrossBuild := true

scmInfo := Some(ScmInfo(
  url(s"https://github.com/ThoughtWorksInc/${name.value}"),
  s"scm:git:git://github.com/ThoughtWorksInc/${name.value}.git",
  Some(s"scm:git:git@github.com:ThoughtWorksInc/${name.value}.git")))

licenses += "Apache" -> url("http://www.apache.org/licenses/LICENSE-2.0")
