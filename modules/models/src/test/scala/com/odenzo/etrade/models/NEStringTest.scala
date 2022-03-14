package com.odenzo.etrade.models

import io.circe.Decoder.Result
import munit.FunSuite

class NEStringTest extends FunSuite {
  import io.circe.*
  import io.circe.syntax.*
  import com.odenzo.etrade.models.opaques.*
  import com.odenzo.etrade.models.codecs.given

  test("NEString Construct") {
    val foo: NEString = NEString("")
    val fof: NEString = NEString("   \t\n")
    val bar: NEString = NEString("Anpiwejfwie")
    val car: NEString = NEString.fromOption(Some("sooon"))
    val aar: NEString = NEString.fromOption(None)
    val eek: NEString = NEString(null)

    scribe.info(s"Foo $foo $fof $bar $car $aar $eek")
  }

  test("Decooding Hello") {
    val x: Result[NEString] = Json.fromString("Hello").as[NEString]
    scribe.info("Done")
    scribe.info(s"$x")
  }
//
//  test("Decooding Blank") {
//    Json.fromString("  ").as[NEString]
//  }
}
