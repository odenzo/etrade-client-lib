import sbt.{Def, _}

object V {

  val blindSightLog      = "1.4.0"
  val catsEffect         = "3.3.7"
  val catsMice           = "1.0.10"
  val catsRetry          = "3.1.0"
  val cats               = "2.7.0"
  val catsCollections    = "0.9.3"
  val circeGenericExtras = "0.15.0-M1"
  val circeOptics        = "0.15.0-M1"
  val circe              = "0.15.0-M1"
  val doobie             = "1.0.0-RC2"
  val fs2                = "3.2.5"
  val http4s             = "1.0.0-M31"
  val logback            = "1.2.11"
  val monocle            = "3.1.0"
  val munitCats          = "1.0.7"
  val munit              = "1.0.0-M2"
  val oslib              = "0.8.1"
  val pprint             = "0.7.2"
  val scalaXML           = "2.0.1"
  val scodecBits         = "1.18"
  val scodecCats         = "1.0.0"
  val scodec             = "1.11.7"
  val scribe             = "3.8.2"
  val squants            = "1.7.0"
  val sttpClient         = "3.5.1"
  val tapir              = "0.19.3"
  val scalaCache         = "1.0.0-M6"
  val scaffeine          = "5.1.2"
}

object Libs {

  val scaffeine = Seq("com.github.blemale" %% "scaffeine" % V.scaffeine % "compile")

  val testing = Seq(
    "org.scalameta" %% "munit"               % V.munit     % Test,
    "org.typelevel" %% "munit-cats-effect-3" % V.munitCats % Test
  )

  val cats = Seq(
    "org.typelevel" %% "cats-core"   % V.cats,
    "org.typelevel" %% "cats-effect" % V.catsEffect
  )

  val catsExtra = Seq(
    "com.github.cb372" %% "cats-retry" % V.catsRetry,
    "org.typelevel"    %% "mouse"      % V.catsMice
  )

  val fs2 = Seq(
    "co.fs2" %% "fs2-core" % V.fs2,
    "co.fs2" %% "fs2-io"   % V.fs2 // No JS Stubbing
  )

  val monocle = Seq(
    "dev.optics" %% "monocle-core"  % V.monocle,
    "dev.optics" %% "monocle-macro" % V.monocle
  )

  val logback = Seq(
    "ch.qos.logback" % "logback-classic" % V.logback
  )

  /** Currently this is only for the binary serialization */
  //
  val standard = Seq(
    "com.lihaoyi" %% "pprint" % V.pprint,
    "com.lihaoyi" %% "os-lib" % V.oslib
  )

  // This IS NOW supposed to work with ScalaJS
  val scalaXML = Seq("org.scala-lang.modules" %% "scala-xml" % V.scalaXML)

  val tapir = Seq(
    "com.softwaremill.sttp.tapir" %% "tapir-core"          % V.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-json-circe"    % V.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-circe" % V.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-openapi-docs"  % V.tapir,
    "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % V.tapir
  )

  val sttpClient = Seq(
    "com.softwaremill.sttp.client3" %% "core"  % V.sttpClient,
    "com.softwaremill.sttp.client3" %% "circe" % V.sttpClient
  )

  val scribe = Seq("com.outr" %% "scribe-slf4j" % V.scribe)

  val circe = Seq(
    "io.circe" %% "circe-core"    % V.circe,
    "io.circe" %% "circe-generic" % V.circe,
    "io.circe" %% "circe-extras"  % V.circe,
    "io.circe" %% "circe-jawn"    % V.circe,
    "io.circe" %% "circe-numbers" % V.circe,
    "io.circe" %% "circe-parser"  % V.circe,
    "io.circe" %% "circe-pointer" % V.circe
    //  "io.circe" %% "circe-generic-extras" % circeGenericExtrasVersion,
    // "io.circe" %% "circe-optics" % circeOpticsVersion
    // "io.circe" %% "circe-literal" % circeVersion
    // "io.circe" %% "circe-scodec" % circeVersion,
    // "io.circe" %% "circe-fs2" % circeVersion
  )

  /** HTTP4S on the backend only */
  val http4s = Seq(
    //  "io.circe"       %% "circe-spire"          % "0.1.0",   Meh, stuck at 2.12
    "org.http4s" %% "http4s-dsl"          % V.http4s,
    "org.http4s" %% "http4s-blaze-server" % V.http4s,
    "org.http4s" %% "http4s-blaze-client" % V.http4s,
    "org.http4s" %% "http4s-circe"        % V.http4s,
    "org.http4s" %% "http4s-scala-xml"    % V.http4s
    // "org.http4s" %% "http4s-scalatags" % http4sVersion,
    // "org.http4s" %% "http4s-jdk-http-client" % "0.3.5"
  )
  //
  val doobie = Seq(
    // Start with this one     (skunk?)
    "org.tpolecat" %% "doobie-core"      % V.doobie,
    "org.tpolecat" %% "doobie-hikari"    % V.doobie,
    "org.tpolecat" %% "doobie-postgres"  % V.doobie,
    // "org.tpolecat" %% "doobie-quill"     % V.doobie, // https://github.com/polyvariant/doobie-quill
    "org.tpolecat" %% "doobie-scalatest" % V.doobie % Test
  )
  // https://mvnrepository.com/artifact/com.github.cb372/scalacache-core

  val scalacache = Seq("com.github.cb372" %% "scalacache-core" % V.scalaCache)
  //  Seq(
  //    "com.tersesystems.blindsight"  %% "blindsight-logstash"  % blindSightLogV,
  //    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.1"
  //  )

  val all: Seq[ModuleID] = testing ++ cats ++ fs2 ++ monocle ++ standard ++ tapir ++ sttpClient ++ scribe ++ circe ++ http4s ++ doobie
}

object XLibs {

  import sbt.{Def, _}
  import org.portablescala.sbtplatformdeps.PlatformDepsPlugin.autoImport._

  lazy val scalaXML        = Def.setting("org.scala-lang.modules" %%% "scala-xml" % V.scalaXML)
  lazy val cats            = Def.setting("org.typelevel" %%% "cats-core" % V.cats)
  lazy val catsEffect      = Def.setting("org.typelevel" %%% "cats-effect" % V.catsEffect)
  lazy val catsCollections = Def.setting("org.typelevel" %%% "cats-collections-core" % V.cats)
  lazy val monocle         = Def.setting("dev.optics" %%% "monocle-core" % V.monocle)
  lazy val pPrint          = Def.setting("com.lihaoyi" %%% "pprint" % V.pprint)
  lazy val scribe          = Def.setting("com.outr" %%% "scribe" % V.scribe)
  lazy val munit           = Def.setting("org.scalameta" %%% "munit" % V.munit % Test)
  lazy val scodecBits      = Def.setting("org.scodec" %%% "scodec-bits" % V.scodecBits)
  lazy val munitCats       = Def.setting("org.typelevel" %%% "munit-cats-effect-3" % V.munitCats % Test)
  lazy val defaultLibs     = Seq(cats, catsEffect, catsCollections, monocle, scribe, munit, munitCats)

  lazy val circeCore    = Def.setting("io.circe" %%% "circe-core" % V.circe)
  lazy val circeGeneric = Def.setting("io.circe" %%% "circe-generic" % V.circe)
  lazy val circePointer = Def.setting("io.circe" %%% "circe-pointer" % V.circe)
  lazy val circeParser  = Def.setting("io.circe" %%% "circe-parser" % V.circe)
  lazy val circeNumbers = Def.setting("io.circe" %%% "circe-numbers" % V.circe)
//  lazy val lCirceYaml       = Def.setting("io.circe" %%% "circe-yaml" % V.circe)
//  lazy val lCirceRefined    = Def.setting("io.circe" %%% "circe-refined" % V.circe)
  // lazy val lCirceExtra      = Def.setting("io.circe" %%% "circe-extras" % V.circe)

  lazy val fs2 = Def.setting("co.fs2" %%% "fs2-core" % V.fs2) // fs-io is not xplatform, to JS at least.

  lazy val http4sCore = Def.setting("org.http4s" %%% "http4s-core" % V.http4s)
  lazy val http4sDsl  = Def.setting("org.http4s" %%% "http4s-dsl" % V.http4s)

  // lazy val http4sClient = Def.setting("org.http4s" %%% "http4s-blaze-client" % V.http4s) .. Bi ScalaJS
  lazy val http4sEmber = Def.setting("org.http4s" %%% "http4s-ember-client" % V.http4s) // Diff with Blaze, use different on JVM or JS
  lazy val http4sCirce = Def.setting("org.http4s" %%% "http4s-circe" % V.http4s)
  //  lazy val http4sXml   = Def.setting("org.http4s" %%% "http4s-scala-xml" % V.http4s) // No ScalJS Version of htto4s stuff

  // val http4sJS = Seq(XLibs.http4Dsl, XLibs.http4sClient, XLibs.http4sCirce, http4sXml)

  // "org.http4s" %% "http4s-scalatags" % http4sVersion,
  // "org.http4s" %% "http4s-jdk-http-client" % "0.3.5"

  // val lCirceScodec: Def.setting     = Def.setting("io.circe", "circe-scodec", circeVersion) // There
  // val lScodecCore = OModule("org.scodec", "scodec-core", scodecV)
  //  val lScodecBits = OModule("org.scodec" , "scodec-bits" , scodecBitsV) // Use this in more places
  //  val lScodecCats = OModule("org.scodec" , "scodec-cats" , scodecCatsV)
  //  lazy val catsRetru: Def.setting = Def.setting("com.github.cb372", "cats-retry", V.catsRetry)
//  val lSquants = OModule("org.typelevel" %%% "squants" % squantsV)

  //   val lScalaJavaTime     = OModule("io.github.cquiroz" %%% "scala-java-time" % scalaJavaTime)
  //   val lScalaJavaTimeTZDB = OModule("io.github.cquiroz" %%% "scala-java-time-tzdb" % scalaJavaTime)
//

  //
  //
  //  val lScalaTags    = Def.setting("com.lihaoyi" %%% "scalatags" % scalaTagsV)
  //  val lScalaCss     = Def.setting("com.github.japgolly.scalacss" %%% "core" % scalaCssV)
  //  val lScalaTagsCss = Def.setting("com.github.japgolly.scalacss" %%% "ext-scalatags" % scalaCssV)
  //
}
