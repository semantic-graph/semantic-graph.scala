name := "semantic-graph"

ThisBuild / organization := "com.semantic_graph"
ThisBuild / version      := "0.1-SNAPSHOT"

version := "0.1"

scalaVersion := "2.13.1"

libraryDependencies += "junit" % "junit" % "4.8.1" % "test"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.0"
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
libraryDependencies += "com.github.scopt" %% "scopt" % "4.0.0-RC2"
libraryDependencies += "guru.nidi" % "graphviz-java" % "0.12.1"
libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.6.2"
libraryDependencies += "org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0"
libraryDependencies += "it.uniroma1.dis.wsngroup.gexf4j" % "gexf4j" % "1.0.0"

publishM2Configuration := publishM2Configuration.value.withOverwrite(true)

mainClass := Some("com.semantic_graph.Main")
