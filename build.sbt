import sbt.Keys.packageSrc
import sbt._
import sbt.util.FileInfo.hash

//ThisBuild / bspEnabled := false
ThisBuild / resolvers += Resolver.mavenLocal
val javart = "1.11"

ThisBuild / scalaVersion  := "3.1.1"
ThisBuild / organization  := "com.odenzo"
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / homepage := Some(url("https://github.com/odenzo/etrade-client-lib"))
ThisBuild / licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0"))

//TokenSource.Environment("GITHUB_TOKEN"),
ThisBuild / publishMavenStyle.withRank(KeyRanks.Invisible) := true

ThisBuild / githubOwner      := "odenzo"
ThisBuild / githubRepository := "etrade-client-lib"

resolvers += Resolver.githubPackages("odenzo")

root / Compile / mainClass := Some("com.odenzo.etrade.Main")
Test / fork                := true
Test / parallelExecution   := false
Test / logBuffered         := false

/** Scala 3 Scala Docs */
ThisBuild / apiURL          := Some(url("https://odenzo.com/etrade-client/api/"))
ThisBuild / autoAPIMappings := true

ThisBuild / scalacOptions := Seq("-release", "11")
ThisBuild / scalacOptions ++= Scala3Settings.settings
ThisBuild / scalacOptions ++= Seq("-Xmax-inlines", "512")

val publishSettings = Seq(
  Test / packageBin / publishArtifact := false,
  Test / packageDoc / publishArtifact := false,
  Test / packageSrc / publishArtifact := true
)

lazy val noPublishSettings = Seq(publishArtifact := false)

lazy val root = project
  .in(file("."))
  .aggregate(models.js, models.jvm, apis.js, apis.jvm, server.jvm, pureBackend)
  .withId("etrade")
  .settings(
    name           := "etrade",
    publish / skip := true
  )

lazy val models = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/models"))
  // .withId("models")
  .settings(name := "etrade-models", libraryDependencies ++= Seq(XLibs.http4sCore.value, XLibs.http4sCirce.value))
  .settings(
    libraryDependencies ++= Seq(XLibs.munit.value, XLibs.munitCats.value),
    libraryDependencies ++= Seq(
      XLibs.circeCore.value,
      XLibs.circeParser.value,
      XLibs.circeGeneric.value,
      XLibs.circePointer.value,
      XLibs.circeTest.value,
      XLibs.monocle.value,
      XLibs.scribe.value,
      XLibs.pPrint.value,
      XLibs.cats.value,
      XLibs.catsEffect.value,
      XLibs.fs2.value,
      XLibs.http4sClient.value,
      XLibs.http4sServer.value,
      XLibs.scalaXML.value,
      XLibs.scalaJavaTime.value,
      XLibs.scalaJavaTimeTZDB.value,
      "com.odenzo" %%% "http4s-dom-xml" % "0.0.4",
      XLibs.http4sCirce.value
    )
  ) //
  .jvmSettings()
  .jsSettings(libraryDependencies += ("org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0").cross(CrossVersion.for3Use2_13))

lazy val apis = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/apis"))
  .dependsOn(models % "compile->compile;test->test")
  .settings(
    name := "etrade-apis",
    libraryDependencies ++= Seq(
      XLibs.http4sCore.value,
      XLibs.http4sDsl.value,
      XLibs.http4sCirce.value,
      XLibs.munit.value,
      XLibs.munitCats.value,
      XLibs.http4sCore.value,
      XLibs.http4sDsl.value,
      XLibs.http4sCirce.value
    )
  )
  .jvmSettings(libraryDependencies ++= Libs.monocle ++ Libs.http4s ++ Libs.catsExtra ++ Libs.fs2)
  .jvmSettings(libraryDependencies ++= Libs.standard ++ Libs.http4s)
  .jsSettings(libraryDependencies ++= Seq(JSLibs.http4sDom.value))

lazy val server = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/server"))
  .dependsOn(models, apis)
  .settings(
    name := "etrade-server",
    libraryDependencies ++= Seq(
      XLibs.munit.value,
      XLibs.munitCats.value,
      XLibs.http4sCore.value,
      XLibs.http4sDsl.value,
      XLibs.http4sCirce.value
    )
  )
  .jvmSettings(libraryDependencies ++= Libs.standard ++ Libs.http4s)

lazy val pureBackend = project
  .in(file("app/backend_it"))
  .dependsOn(apis.jvm % "compile->compile;test->test", server.jvm)
  .settings(
    libraryDependencies ++= Libs.testing ++ Libs.scribeSLF4J ++ Libs.http4s ++ Libs.standard,
    mainClass   := Some("com.odenzo.etradeapp.purebackend.PBEnd"),
    Test / fork := true
  )

addCommandAlias("make-docs", "clean;")

addCommandAlias("erun", "example/run")
