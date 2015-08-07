disablePlugins(HaxeCSharpPlugin)

organization := "com.thoughtworks"

name := "rest-rpc-play"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.6.4" % Test

libraryDependencies += "com.qifun" %% "json-stream" % "0.2.3" % HaxeJava classifier "haxe-java"

libraryDependencies += "com.qifun" %% "json-stream" % "0.2.3" % Provided

libraryDependencies += "org.scala-stm" %% "scala-stm" % "0.7" % Test

libraryDependencies += "com.qifun" %% "haxe-scala-stm" % "0.1.4" % HaxeJava classifier "haxe-java"

haxeOptions in Test ++= Seq("-lib", "continuation")

haxeOptions in Test ++= Seq("-D", "scala")

scalacOptions in Test += "-Yrangepos"