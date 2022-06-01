package com.odenzo.etrade.models.utils

import io.circe.*
import io.circe.Decoder.{Result, const}
import io.circe.JsonObject.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import munit.FunSuite

class CirceUtilsSpec extends munit.CatsEffectSuite with CirceUtils {

  test("Capitalize") {
    assertEquals(capitalize("foo"), "Foo")
    assertEquals(capitalize("Foo"), "Foo")
    assertEquals(capitalize("fOO"), "FOO")
    assertEquals(capitalize("f"), "F")
    assertEquals(capitalize(""), "")
    assertEquals(capitalize(null), null)
  }

  test("UnCapitalize") {
    assertEquals(unCaptialize("Foo"), "foo")
    assertEquals(unCaptialize("FOO"), "fOO")
    assertEquals(unCaptialize("F"), "f")
    assertEquals(unCaptialize(""), "")
    assertEquals(unCaptialize(null), null)
  }

  test("Capitalize Codecs") {
    case class Foo(aaa: String, bbbb: Int, c: Boolean)

    object Foo:
      val decoder: Decoder[Foo]          = deriveDecoder[Foo].prepare(prepareKeys(unCaptialize))
      val encoder: Encoder.AsObject[Foo] = deriveEncoder[Foo].mapJsonObject(encoderTransformKey(capitalize))
      val codec: Codec.AsObject[Foo]     = Codec.AsObject.from(decoder, encoder)

    val data          = Foo("bar", 42, false)
    val encoded: Json = Foo.encoder(data)
    scribe.info(s"Encoded Capitalized: ${encoded.spaces4}")

    val decoded: Result[Foo] = Foo.decoder.decodeJson(encoded)
    scribe.info(s"Decoded: $decoded")

  }

  case class Bar(aaa: String, bbbb: Int, c: Boolean)

  object Bar:
    val rename: Map[String, String]    = Map("aaa" -> "TheFirst", "bbbb" -> "TheSecond", "cIsNotMapped" -> "IsItTrue", "noSuchKey" -> "ERROR")
    val decoder: Decoder[Bar]          = deriveDecoder[Bar].prepare(prepareKeys(mapKeys(reverse(rename))))
    val encoder: Encoder.AsObject[Bar] = deriveEncoder[Bar].mapJsonObject(encoderTransformKey(mapKeys(rename)))
    val codec: Codec.AsObject[Bar]     = Codec.AsObject.from(decoder, encoder)

  test("Renaming Codecs") {
    val data          = Bar("bar", 42, false)
    scribe.info(s"Original Data: $data")
    val encoded: Json = Bar.encoder(data)
    scribe.info(s"Encoded Renamed: ${encoded.spaces4}")

    val decoded: Result[Bar] = Bar.decoder.decodeJson(encoded)
    scribe.info(s"Decoded Renamed $decoded")

  }

}
