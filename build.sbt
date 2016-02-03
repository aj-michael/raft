name := "Raft"
version := "0.1"
scalaVersion := "2.11.7"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.1"
libraryDependencies += "com.typesafe.akka" %% "akka-remote" % "2.4.1"
libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.4.1"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3"
mainClass := Some("edu.rosehulman.client.Application")
