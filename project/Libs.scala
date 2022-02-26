import sbt.{Def, _}

object Libs {

  val testing =
    Seq(
      "org.scalameta" %% "munit"               % Version.munit     % Test,
      "org.typelevel" %% "munit-cats-effect-3" % Version.munitCats % Test
    )

  val cats = Seq(
    "org.typelevel" %% "cats-core"   % Version.cats,
    "org.typelevel" %% "cats-effect" % Version.catsEffect
  )

  val catsExtra = Seq(
    "com.github.cb372" %% "cats-retry" % Version.catsRetry,
    "org.typelevel"    %% "mouse"      % Version.catsMice
  )

  val fs2 = Seq(
    "co.fs2" %% "fs2-core" % Version.fs2,
    "co.fs2" %% "fs2-io"   % Version.fs2
  )

  val monocle = Seq(
    "dev.optics" %% "monocle-core"  % Version.monocle,
    "dev.optics" %% "monocle-macro" % Version.monocle
  )

  /** Currently this is only for the binary serialization */
  //
  val standard = Seq(
    "com.lihaoyi" %% "pprint" % Version.pprint,
    "com.lihaoyi" %% "os-lib" % Version.oslib
  )

  // This IS NOW supposed to work with ScalaJS
  val scalaXML = Seq("org.scala-lang.modules" %% "scala-xml" % Version.scalaXML)

  val tapir = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core"          % Version.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe"    % Version.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe" % Version.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"  % Version.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % Version.tapir
  )

  val sttpClient = Seq(
    "com.softwaremill.sttp.client3" %% "core"  % Version.sttpClient,
    "com.softwaremill.sttp.client3" %% "circe" % Version.sttpClient
  )

  val scribe = Seq("com.outr" %% "scribe" % Version.scribe, "ch.qos.logback" % "logback-classic" % "1.2.10")

  val circe = Seq(
    "io.circe" %% "circe-core"    % Version.circe,
    "io.circe" %% "circe-generic" % Version.circe,
    "io.circe" %% "circe-extras"  % Version.circe,
    "io.circe" %% "circe-jawn"    % Version.circe,
    "io.circe" %% "circe-numbers" % Version.circe,
    "io.circe" %% "circe-parser"  % Version.circe,
    "io.circe" %% "circe-pointer" % Version.circe
    //  "io.circe" %% "circe-generic-extras" % circeGenericExtrasVersion,
    // "io.circe" %% "circe-optics" % circeOpticsVersion
    // "io.circe" %% "circe-literal" % circeVersion
    // "io.circe" %% "circe-scodec" % circeVersion,
    // "io.circe" %% "circe-fs2" % circeVersion
  )

  /** HTTP4S on the backend only */
  val http4s = Seq(
    //  "io.circe"       %% "circe-spire"          % "0.1.0",   Meh, stuck at 2.12
    "org.http4s" %% "http4s-dsl"          % Version.http4s,
    "org.http4s" %% "http4s-blaze-server" % Version.http4s,
    "org.http4s" %% "http4s-blaze-client" % Version.http4s,
    "org.http4s" %% "http4s-circe"        % Version.http4s,
    "org.http4s" %% "http4s-scala-xml"    % Version.http4s
    // "org.http4s" %% "http4s-scalatags" % http4sVersion,
    // "org.http4s" %% "http4s-jdk-http-client" % "0.3.5"
  )
  //
  val doobie = Seq(
    // Start with this one     (skunk?)
    "org.tpolecat" %% "doobie-core"      % Version.doobie,
    "org.tpolecat" %% "doobie-hikari"    % Version.doobie,
    "org.tpolecat" %% "doobie-postgres"  % Version.doobie,
    // "org.tpolecat" %% "doobie-quill"     % Version.doobie, // https://github.com/polyvariant/doobie-quill
    "org.tpolecat" %% "doobie-scalatest" % Version.doobie % "test"
  )

  //  Seq(
  //    "com.tersesystems.blindsight"  %% "blindsight-logstash"  % blindSightLogV,
  //    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.1"
  //  )

  val all: Seq[ModuleID] = testing ++ cats ++ fs2 ++ monocle ++ standard ++ tapir ++ sttpClient ++ scribe ++ circe ++ http4s ++ doobie
}
//
//object LibsX {
//
//  import Version._
//  import OModules.OModule
//
//  lazy val cats             = Def.setting("org.typelevel" %%% "cats-core" % Version.cats)
//  lazy val catsEffect       = Def.setting("org.typelevel" %%% "cats-effect" % Version.catsEffect)
//  lazy val lCatsCollections = Def.setting("org.typelevel" %%% "cats-collections-core" % Version.cats)
//  lazy val monocle          = Def.setting("dev.optics" %%% "monocle-core" % Version.monocle)
//  lazy val lPPrint          = Def.setting("com.lihaoyi" %%% "pprint" % Version.pprint)
//  lazy val lScribe          = Def.setting("com.outr" %%% "scribe" % Version.scribe)
//  lazy val lMunit           = Def.setting("org.scalameta" %%% "munit" % Version.munit % Test)
//  lazy val lScodecBits      = Def.setting("org.scodec" %%% "scodec-bits" % Version.scodecBits)
//  lazy val lMunitCats       = Def.setting("org.typelevel" %%% "munit-cats-effect-3" % Version.munitCats % Test)
//  lazy val defaultLibs      = Seq(cats, catsEffect, lCatsCollections, monocle, lPPrint, lScribe, lMunit, lMunitCats)
//  //
////
////  val lScalaTags    = Def.setting("com.lihaoyi" %%% "scalatags" % scalaTagsV)
////  val lScalaCss     = Def.setting("com.github.japgolly.scalacss" %%% "core" % scalaCssV)
////  val lScalaTagsCss = Def.setting("com.github.japgolly.scalacss" %%% "ext-scalatags" % scalaCssV)
////
//  lazy val lCirceCore       = Def.setting("io.circe" %%% "circe-core" % Version.circe)
//  lazy val lCirceGeneric    = Def.setting("io.circe" %%% "circe-generic" % Version.circe)
//  lazy val lCircePointer    = Def.setting("io.circe" %%% "circe-pointer" % Version.circe)
//  lazy val lCirceParser     = Def.setting("io.circe" %%% "circe-parser" % Version.circe)
//  lazy val lCirceYaml       = Def.setting("io.circe" %%% "circe-yaml" % Version.circe)
//  lazy val lCirceRefined    = Def.setting("io.circe" %%% "circe-refined" % Version.circe)
//  lazy val lCirceExtra      = Def.setting("io.circe" %%% "circe-extras" % Version.circe)
//  lazy val lCirceNumbers    = Def.setting("io.circe" %%% "circe-numbers" % Version.circe)
//
//  lazy val circeStdLibs = Seq(lCirceCore, lCirceGeneric, lCirceParser, lCircePointer, lCirceNumbers)
//  lazy val stdLibs      = defaultLibs ++ circeStdLibs
//
//  //  lazy val catsRetru: Def.setting = Def.setting("com.github.cb372", "cats-retry", Version.catsRetry)
//  //  val fs2: Def.setting            = Def.setting("co.fs2", "fs2-core", Version.fs2) // fs-io is not xplatform, to JS at least.
//  // val lCirceScodec: Def.setting     = Def.setting("io.circe", "circe-scodec", circeVersion) // There
//  // val lScodecCore = OModule("org.scodec", "scodec-core", scodecV)
//  //  val lScodecBits = OModule("org.scodec" , "scodec-bits" , scodecBitsV) // Use this in more places
//  //  val lScodecCats = OModule("org.scodec" , "scodec-cats" , scodecCatsV)
//
////  val lSquants = OModule("org.typelevel" %%% "squants" % squantsV)
////  val lChimney = OModule("io.scalaland" %%% "chimney" % chimneyV)
//  //   val lScalaJavaTime     = OModule("io.github.cquiroz" %%% "scala-java-time" % scalaJavaTime)
//  //   val lScalaJavaTimeTZDB = OModule("io.github.cquiroz" %%% "scala-java-time-tzdb" % scalaJavaTime)
////
//
//}
