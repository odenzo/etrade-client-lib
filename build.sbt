import sbt.Keys.packageSrc
import sbt._
import sbt.util.FileInfo.hash

//ThisBuild / bspEnabled := false

val javart = "1.11"

ThisBuild / scalaVersion  := "3.1.1"
ThisBuild / organization  := "com.odenzo"
ThisBuild / versionScheme := Some("early-semver")

ThisBuild / homepage := Some(url("https://github.com/odenzo/etrade=client-lib"))
ThisBuild / licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

ThisBuild / githubTokenSource.withRank(KeyRanks.Invisible) := TokenSource.Or(
  TokenSource.Environment("GITHUB_TOKEN"),
  TokenSource.GitConfig("github.token")
)

ThisBuild / publishMavenStyle.withRank(KeyRanks.Invisible) := true

ThisBuild / githubOwner      := "odenzo"
ThisBuild / githubRepository := "etrade-client-lib"

resolvers += Resolver.githubPackages("odenzo")
//Compile / doc / scalacOptions ++= {
//  Seq(
//    "-sourcepath",
//    (LocalRootProject / baseDirectory).value.getAbsolutePath,
//    "-doc-source-url",
//    s"https://github.com/sbt/sbt-release/tree/${hash()}€{FILE_PATH}.scala"
//  )
//}

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
  .aggregate(common.js, common.jvm, models.js, models.jvm, client.js, client.jvm, apis.js, apis.jvm, example)
  .withId("etrade")
  .settings(
    name           := "etrade",
    publish / skip := true
  )

lazy val common = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/common"))
  .settings(
    name := "etrade-common",
    libraryDependencies ++= Seq(
      XLibs.circeCore.value,
      XLibs.circeParser.value,
      XLibs.circeGeneric.value,
      XLibs.circePointer.value,
      XLibs.monocle.value,
      XLibs.scribe.value,
      XLibs.pPrint.value,
      XLibs.cats.value,
      XLibs.catsEffect.value,
      XLibs.fs2.value,
      XLibs.http4sClient.value,
      XLibs.http4sServer.value,
      XLibs.scalaXML.value,
      // XLibs.http4sXml.value,
      XLibs.http4sCirce.value
    )
  )
  .settings(libraryDependencies ++= Seq(XLibs.munit.value, XLibs.munitCats.value)) // Tesst
  .jvmSettings().jsSettings()

lazy val models = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/models"))
  // .withId("models")
  .dependsOn(common)
  .settings(name := "etrade-models", libraryDependencies ++= Seq(XLibs.http4sCore.value, XLibs.http4sCirce.value))
  .settings(libraryDependencies ++= Seq(XLibs.munit.value, XLibs.munitCats.value)) //
  .jvmSettings().jsSettings()

lazy val apis = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("modules/apis"))
  .dependsOn(common, models)
  .settings(
    name := "etrade-apis",
    libraryDependencies ++= Seq(XLibs.http4sCore.value, XLibs.http4sDsl.value, XLibs.http4sCirce.value)
  )
  .jvmSettings(libraryDependencies ++= Libs.monocle ++ Libs.http4s ++ Libs.catsExtra ++ Libs.fs2)

lazy val client = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("modules/client"))
  .dependsOn(common, models, apis)
  .settings(
    name := "etrade-client",
    libraryDependencies ++= Seq(
      XLibs.munit.value,
      XLibs.munitCats.value,
      XLibs.http4sCore.value,
      XLibs.http4sDsl.value,
      XLibs.http4sCirce.value
    )
  )
  .jvmSettings(libraryDependencies ++= Libs.standard ++ Libs.http4s)
  .jsSettings(libraryDependencies ++= Seq(JSLibs.http4sDom.value))

lazy val example = project
  .in(file("modules/example"))
  .withId("example")
  .dependsOn(common.jvm, models.jvm, client.jvm, apis.jvm)
  .settings(name := "example-usage")
  .settings(publish / skip := true)
  .settings(libraryDependencies ++= Libs.testing)

addCommandAlias("make-docs", "clean;")
addCommandAlias("ci-test", "+clean;+test -- -DCI=true")
addCommandAlias("erun", "example/run")
