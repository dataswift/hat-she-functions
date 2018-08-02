import sbt.Keys._
import sbt._

name := "hat-she-functions"

resolvers += Resolver.sonatypeRepo("public")
resolvers += "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
resolvers += "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com"

scalaVersion := "2.12.4"
assemblyJarName in assembly := "hat-she-functions.jar"

libraryDependencies ++= Seq(
  "org.hatdex" %% "aws-lambda-scala-handler" % "0.0.2-SNAPSHOT",
  "com.github.seratch" %% "awscala" % "0.6.+",
  "org.specs2" %% "specs2-core" % "4.0.0" % "provided",
  "org.specs2" %% "specs2-matcher-extra" % "4.0.0" % "provided",
  "org.specs2" %% "specs2-mock" % "4.0.0" % "provided",
  "com.typesafe.akka" %% "akka-actor" % "2.5.9",
  "com.typesafe.play" %% "play-ahc-ws-standalone" % "1.1.3",
  "com.typesafe.play" %% "play-ws-standalone-json" % "1.1.3",
  "org.hatdex" %% "hat-client-scala-play" % "2.6.2-SNAPSHOT" excludeAll(
    ExclusionRule("commons-logging", "commons-logging"),
    ExclusionRule(organization = "com.typesafe", name="akka-stream"),
    ExclusionRule(organization = "com.typesafe", name="akka-http"),
    ExclusionRule(organization = "org.specs2"),
    ExclusionRule(organization = "org.seleniumhq.selenium"),
    ExclusionRule(organization = "com.typesafe.play", name="play-akka-http-server")),
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1" classifier "models"
)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case PathList("com", "amazonaws", "services", xs @ _*) if !xs.contains("lambda") => MergeStrategy.discard
  // remove Stanford NLP Demo
  case PathList("edu", "stanford", "nlp", xs @ _*) if xs.contains("demo") => MergeStrategy.discard
  // remove Stanford NLP unused models
  case PathList("edu", "stanford", "nlp", xs @ _*) if xs.contains("coref") => MergeStrategy.discard
  case PathList("edu", "stanford", "nlp", xs @ _*) if xs.contains("dcoref") => MergeStrategy.discard
  case PathList("edu", "stanford", "nlp", xs @ _*) if xs.contains("ner") => MergeStrategy.discard
  case PathList("edu", "stanford", "nlp", xs @ _*) if xs.contains("truecase") => MergeStrategy.discard
  case PathList("edu", "stanford", "nlp", xs @ _*) if xs.contains("kbp") => MergeStrategy.discard
  case PathList("edu", "stanford", "nlp", xs @ _*) if xs.contains("naturalli") => MergeStrategy.discard
  case PathList("edu", "stanford", "nlp", xs @ _*) if xs.contains("supervised_relation_extractor") => MergeStrategy.discard
  case PathList("edu", "stanford", "nlp", xs @ _*) if xs.contains("quoteattribution") => MergeStrategy.discard
  // remove test dependencies
  case PathList("org", "specs2", xs @ _*) => MergeStrategy.discard
  case PathList("org", "seleniumhq", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings")

test in assembly := {}
