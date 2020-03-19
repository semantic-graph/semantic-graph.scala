SCALA_FILES := $(shell find src -name "*.scala")

jar: target/scala-2.13/semantic-graph.scala-assembly-0.2.0.jar

target/scala-2.13/semantic-graph.scala-assembly-0.2.0.jar: $(SCALA_FILES)
	echo $(SCALA_FILES)
	sbt assembly
