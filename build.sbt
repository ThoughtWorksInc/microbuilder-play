disablePlugins(HaxeCSharpPlugin)

organization := "com.thoughtworks"

name := "rest-rpc-play"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.6.4" % Test

libraryDependencies += "com.qifun" %% "json-stream" % "0.2.3" % HaxeJava classifier "haxe-java"

libraryDependencies += "com.qifun" %% "json-stream" % "0.2.3" % Provided

libraryDependencies += "org.scala-stm" %% "scala-stm" % "0.7" % Test

libraryDependencies += "com.qifun" %% "haxe-scala-stm" % "0.1.4" % HaxeJava classifier "haxe-java"

libraryDependencies += "org.specs2" %% "specs2-mock" % "3.6.4"

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19"

libraryDependencies += "com.github.dreamhead" % "moco-core" % "0.10.1" % Test
libraryDependencies += "com.typesafe.play" % "play-ws_2.10" % "2.3.6"

haxeOptions in Test ++= Seq("-lib", "continuation")

haxeOptions in Test ++= Seq("-D", "scala")

scalacOptions in Test += "-Yrangepos"

for (c <- Seq(Compile, Test)) yield {
  haxeOptions in c ++= Seq("-dce", "no")
}