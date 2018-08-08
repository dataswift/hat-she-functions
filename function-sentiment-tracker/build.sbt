import sbt.Keys._
import sbt._

name := "sentiment-tracker"

resolvers += Resolver.sonatypeRepo("public")
resolvers += "HAT Library Artifacts Releases" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-releases.hubofallthings.com"
resolvers += "HAT Library Artifacts Snapshots" at "https://s3-eu-west-1.amazonaws.com/library-artifacts-snapshots.hubofallthings.com"

scalaVersion := "2.12.4"
assemblyJarName in assembly := name.value + ".jar"

libraryDependencies ++= Seq(
  "org.hatdex" %% "aws-lambda-scala-handler" % "0.0.2-SNAPSHOT",
  "org.specs2" %% "specs2-core" % "4.0.0" % "provided",
  "org.specs2" %% "specs2-matcher-extra" % "4.0.0" % "provided",
  "org.specs2" %% "specs2-mock" % "4.0.0" % "provided",
  "org.hatdex" %% "hat-client-scala-play" % "2.6.2-SNAPSHOT" excludeAll(
    ExclusionRule("commons-logging", "commons-logging"),
    ExclusionRule(organization = "com.typesafe", name="akka-stream"),
    ExclusionRule(organization = "com.typesafe", name="akka-http"),
    ExclusionRule(organization = "org.specs2"),
    ExclusionRule(organization = "org.seleniumhq.selenium"),
    ExclusionRule(organization = "com.typesafe.play", name="play-akka-http-server")),
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1"
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
  // merge config files
  case PathList("reference.conf") => MergeStrategy.concat
  case x => MergeStrategy.first
}

scalacOptions ++= Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xfuture",                          // Turn on future language features.
  "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
  "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
  "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
  "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",            // Option.apply used implicit view.
  "-Xlint:package-object-classes",     // Class or object defined in package object.
  "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
  "-Xlint:unsound-match",              // Pattern match may not be typesafe.
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification",             // Enable partial unification in type constructor inference
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",              // Warn if a local definition is unused.
  "-Ywarn-unused:params",              // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",            // Warn if a private member is unused.
  "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
)

test in assembly := {}
