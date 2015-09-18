enablePlugins(HaxeJavaPlugin)

disablePlugins(HaxeCSharpPlugin)

organization := "com.thoughtworks"

name := "rest-rpc-play"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/releases"

libraryDependencies += "org.specs2" %% "specs2-core" % "3.6.4" % Test

libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.4.2"

libraryDependencies += "com.qifun" %% "json-stream" % "0.2.3" % HaxeJava classifier "haxe-java"

libraryDependencies += "com.qifun" %% "json-stream" % "0.2.3" % Provided

libraryDependencies += "org.scala-stm" %% "scala-stm" % "0.7" % Test

libraryDependencies += "com.qifun" %% "haxe-scala-stm" % "0.1.4" % HaxeJava classifier "haxe-java"

libraryDependencies += "org.specs2" %% "specs2-mock" % "3.6.4"

libraryDependencies += "org.mockito" % "mockito-all" % "1.10.19"

libraryDependencies += "com.github.dreamhead" % "moco-core" % "0.10.1"

libraryDependencies += "de.leanovate.play-mockws" %% "play-mockws" % "2.4.0" % Test

libraryDependencies += "com.typesafe.play" %% "play-specs2" % "2.4.2" % Test

for (c <- Seq(Compile, Test)) yield {
  haxeOptions in c ++= Seq("-lib", "continuation")
}

for (c <- Seq(Compile, Test)) yield {
  haxeOptions in c ++= Seq("-D", "scala")
}

scalacOptions in Test += "-Yrangepos"

compileOrder := CompileOrder.JavaThenScala

for (c <- Seq(Compile, Test)) yield {
  haxeOptions in c ++= Seq("-dce", "no")
}

for (c <- AllTestTargetConfigurations) yield {
  haxeMacros in c += """com.dongxiguo.autoParser.AutoParser.BUILDER.defineMacroClass([ "com.thoughtworks.restRpc.core.UriTemplate" ], "com.thoughtworks.restRpc.core.UriTemplateParser")"""
}

for (c <- AllTestTargetConfigurations) yield {
  haxeMacros in c += """com.dongxiguo.autoParser.AutoFormatter.BUILDER.defineMacroClass([ "com.thoughtworks.restRpc.core.UriTemplate" ], "com.thoughtworks.restRpc.core.UriTemplateFormatter")"""
}
