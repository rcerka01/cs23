ThisBuild / scalaVersion := "3.2.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "cs.dispatch"
ThisBuild / organizationName := "Clear Score"

lazy val root = (project in file("."))
  .settings(
    name := "ClearScore23",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.9.0",
      "dev.zio" %% "zio" % "2.0.10",
      "dev.zio" %% "zio-json" % "0.4.2",
      "dev.zio" %% "zio-logging" % "2.1.11",
      "dev.zio" %% "zio-config-magnolia" % "3.0.7",
      "dev.zio" %% "zio-config-typesafe" % "3.0.7",
      "dev.zio" %% "zio-logging" % "2.1.11",
      "dev.zio" %% "zio-http" % "3.0.0-RC1",
      "com.softwaremill.sttp.tapir" %% "tapir-core" % "1.4.0",
      "com.softwaremill.sttp.tapir" %% "tapir-zio-http-server" % "1.4.0",
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % "1.4.0",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % "1.4.0",
      "org.mock-server" % "mockserver-netty" % "5.14.0" % Test,
      "dev.zio" %% "zio-test" % "2.0.10" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.10" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )

testOptions += Tests.Argument(TestFrameworks.ScalaTest, "-oD", "-z", "zio")
