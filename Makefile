GITHASH:=$(shell git hash)

SCALA_FILES:=$(shell find src -name "*.scala")
VERSION:=$(shell grep version build.sbt | sed 's/^version.*\"\(.*\)\"/\1/')

JAR:=target/scala-2.13/semantic-graph.scala-assembly-$(VERSION).jar
GITHASH_FILE:=$(JAR).gitver

update-gitver:
	echo $(GITHASH) > $(GITHASH_FILE)

jar: $(JAR) update-gitver

.phony: update-gitver jar

target/scala-2.13/semantic-graph.scala-assembly-$(VERSION).jar: $(SCALA_FILES)
	sbt assembly
