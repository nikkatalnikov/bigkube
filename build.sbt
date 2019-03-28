name := "bigkube"
version := "0.1"
scalaVersion := "2.11.12"

val sparkVersion = "2.4.0"
val circeVersion = "0.11.0"

lazy val IntegrationTest = config("it") extend Test
lazy val root = Project(id = "root", base = file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings: _*)

resolvers += "Confluent Maven Repository" at "https://packages.confluent.io/maven/"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.7"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.7"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-streaming" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-hive" % sparkVersion % "provided",

  "io.reactivex" %% "rxscala" % "0.26.5",

  "com.facebook.presto" % "presto-jdbc" % "0.151" % Test,
  "com.microsoft.azure" % "azure-sqldb-spark" % "1.0.2",
  "com.microsoft.sqlserver" % "mssql-jdbc" % "7.2.0.jre8",
  "com.typesafe.slick" %% "slick" % "3.3.0",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.0",

  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
  "com.typesafe" % "config" % "1.0.2",
  "io.fabric8" % "kubernetes-client" % "4.1.3"  % Test,
  "com.lihaoyi" %% "pprint" % "0.5.3" % Test,
  "org.json" % "json" % "20180813" % Test,

  "org.scalatest" %% "scalatest" % "3.2.0-SNAP10" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test,

  "org.apache.logging.log4j" % "log4j-core" % "2.7",
  "org.apache.logging.log4j" % "log4j-api" % "2.7",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,

  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion exclude ("org.apache.kafka","kafka"),
  "org.apache.kafka" %% "kafka" % "2.1.0",

  "com.sksamuel.avro4s" %% "avro4s-core" % "2.0.2",

  "com.github.azakordonets" % "fabricator_2.11" % "2.1.5" % Test
)

assemblyMergeStrategy in assembly := {
  case PathList("application.conf") => MergeStrategy.concat
  case PathList("git.properties") => MergeStrategy.first
  case PathList("org", "apache", "spark", "unused", "UnusedStubClass.class") => MergeStrategy.first
  case PathList("org", "apache", "commons", "beanutils", _*) => MergeStrategy.first
  case PathList("org", "apache", "commons", "collections", _*) => MergeStrategy.first
  case PathList("org", "apache", "commons", "lang",  _*) => MergeStrategy.first
  case PathList("org", "aopalliance", _*) => MergeStrategy.first
  case PathList("javax", "inject", _*) => MergeStrategy.first
  case PathList("org", "apache", "hadoop", "yarn", _*) => MergeStrategy.first
  case PathList("io", "sundr", _*) => MergeStrategy.first
  case PathList("org", "bouncycastle", _*) => MergeStrategy.first
  case x => (assemblyMergeStrategy in assembly).value(x)
}