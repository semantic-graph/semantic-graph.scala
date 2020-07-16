SCALA_FILES := $(shell find src -name "*.scala")
VERSION=$(shell grep version build.sbt | sed 's/^version.*\"\(.*\)\"\r/\1/')

jar: target/scala-2.13/semantic-graph.scala-assembly-$(VERSION).jar

target/scala-2.13/semantic-graph.scala-assembly-$(VERSION).jar: $(SCALA_FILES)
	sbt assembly
