
name := "AkkaGraphDB"

version := "0.1"

scalaVersion := "2.13.1"

val akkaVersion = "2.6.1"
val akkaManagementVersion = "1.0.4"
val akkaActorTyped = "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion
val akkaPersistenceTyped = "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion
val akkaStreams = "com.typesafe.akka" %% "akka-stream" % akkaVersion
val akkaShardingTyped = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion
val akkaClusterTyped = "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion
val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
val akkaPersistenceQuery = "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion
val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.101"
val akkaManagement = "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion
val akkaManagementClusterHttp = "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion
val akkaManagementClusterBootstrap = "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % akkaManagementVersion
val akkaDiscoveryK8sApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion
val akkaDiscovery = "com.typesafe.akka" %% "akka-discovery" % akkaVersion
val akkaSerialization = "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion
val alpakkaCassandra = "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % "1.1.2"
val ficus = "com.iheart" %% "ficus" % "1.4.3"
val akkaHttp = "com.typesafe.akka" %% "akka-http" % "10.1.10"
val akkaHttpJson4s = "de.heikoseeberger" %% "akka-http-json4s" % "1.29.1"
val json4sNative = "org.json4s" %% "json4s-native" % "3.5.4"
val json4sExt = "org.json4s" %% "json4s-ext" % "3.5.4"
val slf4j = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val leveldb = "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"

val commonDependencies = Seq(
  akkaActorTyped,
  akkaPersistenceTyped,
  akkaHttp,
  akkaShardingTyped,
  akkaClusterTyped,
  akkaClusterTools,
  akkaPersistenceCassandra,
  akkaPersistenceQuery,
  akkaManagement,
  akkaManagementClusterHttp,
  akkaManagementClusterBootstrap,
  akkaDiscovery,
  akkaSerialization,
  akkaStreams,
  alpakkaCassandra,
  ficus,
  akkaHttpJson4s,
  json4sNative,
  json4sExt,
  scalaTest,
  logback,
  scalaTest
)

lazy val `graph` = (project in file("graph"))
  .settings(Seq(
    name := "graph",
    credentials in ThisBuild += Credentials(Path.userHome / ".lightbend" / "commercial.credentials"),
    resolvers in ThisBuild += "lightbend-commercial-maven" at "https://repo.lightbend.com/commercial-releases",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.12.8",
    cinnamon in run := true,
    dockerExposedPorts ++= Seq(9001, 9999, 2551),
    libraryDependencies ++= commonDependencies,
    libraryDependencies ++= Seq(
      Cinnamon.library.cinnamonAkka,
      Cinnamon.library.cinnamonAkkaPersistence,
      Cinnamon.library.cinnamonAkkaHttp,
      Cinnamon.library.cinnamonJvmMetricsProducer,
      Cinnamon.library.cinnamonPrometheus,
      Cinnamon.library.cinnamonPrometheusHttpServer,
      Cinnamon.library.cinnamonOpenTracing,
      Cinnamon.library.cinnamonOpenTracingJaeger
    ),
    dependencyOverrides += "com.google.guava" % "guava" % "19.0",
  )
).enablePlugins(JavaAppPackaging).enablePlugins(DockerPlugin).enablePlugins(Cinnamon)

lazy val gatlingVersion = "3.2.1"
val `benchmarks` = Project(id = "benchmarks",
  base = file("benchmarks"))
  .enablePlugins(GatlingPlugin)
  .settings(
    libraryDependencies ++= Seq(
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion,
      "io.gatling" % "gatling-test-framework" % gatlingVersion,
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.7.0",
      "com.fasterxml.uuid" % "java-uuid-generator" % "3.2.0",
      "io.prometheus" % "simpleclient" % "0.8.0",
      "io.prometheus" % "simpleclient_httpserver" % "0.8.0",
      "com.typesafe.akka" %% "akka-actor" % "2.5.25"
    )
  )
  .dependsOn(`graph`)