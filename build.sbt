enablePlugins(HaxeJavaPlugin)

organization := "com.thoughtworks.microbuilder"

name := "microbuilder-play"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.6.4" % Test

libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.4.2"

libraryDependencies += "com.thoughtworks.microbuilder" % "microbuilder-core" % "3.0.1" % TestHaxeJava classifier "haxe-java"

libraryDependencies += "com.thoughtworks.microbuilder" % "microbuilder-core" % "3.0.1"

libraryDependencies += "com.thoughtworks.microbuilder" % "json-stream-core" % "3.0.2" % TestHaxeJava classifier "haxe-java"

libraryDependencies += "com.thoughtworks.microbuilder" % "json-stream-core" % "3.0.2" % Provided

libraryDependencies += "com.qifun" %% "haxe-scala-stm" % "0.1.4" % TestHaxeJava classifier "haxe-java"

libraryDependencies += "org.scala-stm" %% "scala-stm" % "0.7" % Test

libraryDependencies += "org.specs2" %% "specs2-mock" % "3.6.4" % Test

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19" % Test

libraryDependencies += "com.github.dreamhead" % "moco-core" % "0.10.1" % Test

libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.4.0" % Test

libraryDependencies += "com.typesafe.play" %% "play-specs2" % "2.4.2" % Test

libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value

libraryDependencies ++= {
  if (scalaBinaryVersion.value == "2.10") {
    Seq("org.scalamacros" %% "quasiquotes" % "2.1.0")
  } else {
    Nil
  }
}

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

val haxelibs = Map(
  "continuation" -> DependencyVersion.SpecificVersion("1.3.2"),
  "microbuilder-HUGS" -> DependencyVersion.SpecificVersion("2.0.1")
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

releaseUseGlobalVersion := false

releaseCrossBuild := true

scmInfo := Some(ScmInfo(
  url(s"https://github.com/ThoughtWorksInc/${name.value}"),
  s"scm:git:git://github.com/ThoughtWorksInc/${name.value}.git",
  Some(s"scm:git:git@github.com:ThoughtWorksInc/${name.value}.git")))

licenses += "Apache" -> url("http://www.apache.org/licenses/LICENSE-2.0")

releaseProcess := {
  releaseProcess.value.patch(releaseProcess.value.indexOf(pushChanges), Seq[ReleaseStep](releaseStepCommand("sonatypeRelease")), 0)
}

releaseProcess -= runClean

releaseProcess -= runTest
