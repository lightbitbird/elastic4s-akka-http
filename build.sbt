name := "elastic4s-akka-http"

version := "0.1"

scalaVersion := "2.12.4"

javaOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalacOptions += "-deprecation"

val elastic4sVersion = "6.1.4"

val log4j = "2.9.1"

libraryDependencies ++= {
  val akkaHttp = "10.0.11"
  val scalaTest = "3.0.1"

  Seq(
    "com.typesafe.akka"           %% "akka-http-core"       % akkaHttp,
    "com.typesafe.akka"           %% "akka-http"            % akkaHttp,

    "org.scalatest"               %% "scalatest"            % scalaTest % "test",
    "com.typesafe.scala-logging"  %% "scala-logging"        % "3.5.0",
    "com.typesafe.akka"           %% "akka-http-spray-json" % "10.0.11",
    "com.sksamuel.elastic4s"      %% "elastic4s-jackson"      % elastic4sVersion,
    "org.json4s"                  %% "json4s-native"        % "3.5.3",

    "ch.qos.logback"              % "logback-classic"       % "1.2.3",
    "com.typesafe.akka"           %% "akka-slf4j"           % "2.4.13",
    "org.slf4j" % "log4j-over-slf4j" % "1.7.25",

    "com.sksamuel.elastic4s"      %% "elastic4s-core"       % elastic4sVersion,
    "com.sksamuel.elastic4s"      %% "elastic4s-tcp"        % elastic4sVersion,
    "com.sksamuel.elastic4s"      %% "elastic4s-http"       % elastic4sVersion,
//    "com.sksamuel.elastic4s"  %% "elastic4s-streams"       % elastic4sVersion,
    "com.sksamuel.elastic4s"      %% "elastic4s-testkit"    % elastic4sVersion % "test",
    "com.sksamuel.elastic4s"      %% "elastic4s-embedded"   % elastic4sVersion % "test"
  )

}
