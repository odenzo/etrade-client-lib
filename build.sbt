import sbt._

ThisBuild / bspEnabled := false

val javart = "1.11"

ThisBuild / scalaVersion              := "3.1.1"
ThisBuild / organization              := "com.odenzo"
ThisBuild / dependencyAllowPreRelease := true

root / Compile / mainClass := Some("com.odenzo.etrade.app.ETradeMain")
Test / fork                := true
Test / parallelExecution   := false
Test / logBuffered         := false

ThisBuild / scalacOptions := Seq("-release", "11")
ThisBuild / scalacOptions ++= Scala3Settings.settings

lazy val root = project
  .in(file("."))
  .withId("etrade")
  .aggregate(common, models, client)
  .settings(name := "etrade", doc / aggregate := false)

lazy val common = project
  .in(file("modules/common"))
  .withId("common")
  .settings(name := "common")
  .settings(libraryDependencies ++= Libs.standard ++ Libs.monocle ++ Libs.circe ++ Libs.cats ++ Libs.catsExtra ++ Libs.scribe ++ Libs.fs2)
  .settings(libraryDependencies ++= Libs.testing)

lazy val models = project
  .in(file("modules/models"))
  .withId("models")
  .dependsOn(common)
  .settings(name := "models")
  .settings(libraryDependencies ++= Libs.standard ++ Libs.monocle ++ Libs.circe ++ Libs.cats ++ Libs.catsExtra ++ Libs.scribe ++ Libs.fs2)
  .settings(libraryDependencies ++= Libs.testing)

lazy val client = project
  .in(file("modules/client"))
  .withId("client")
  .dependsOn(common, models, oauth)
  .settings(name := "client")
  .settings(libraryDependencies ++= Libs.monocle ++ Libs.http4s ++ Libs.catsExtra ++ Libs.fs2)
  .settings(libraryDependencies ++= Libs.testing)

lazy val oauth = project
  .in(file("modules/oauth"))
  .withId("oauth")
  .dependsOn(common, models)
  .settings(name := "oauth-callback-server")
  .settings(libraryDependencies ++= Libs.monocle ++ Libs.http4s ++ Libs.catsExtra ++ Libs.fs2)
  .settings(libraryDependencies ++= Libs.scaffeine)
  .settings(libraryDependencies ++= Libs.testing)

lazy val example = project
  .in(file("modules/example"))
  .withId("example")
  .dependsOn(common, models, oauth)
  .settings(name := "example-usage")
  .settings(libraryDependencies ++= Libs.monocle ++ Libs.http4s ++ Libs.catsExtra ++ Libs.fs2)
  .settings(libraryDependencies ++= Libs.testing)

addCommandAlias("ci-test", "+clean;+test -- -DCI=true")
