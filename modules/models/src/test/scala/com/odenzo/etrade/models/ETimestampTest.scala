package com.odenzo.etrade.models

import com.odenzo.base.OPrint.oprint
import io.circe.Decoder.Result
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Codec, Decoder, Json, JsonObject, ParsingFailure}
import munit.FunSuite

import javax.tools.ForwardingFileObject

class ETimestampTest extends FunSuite {

  val jsonTxt: String =
    """{
      |  "foo" : 1646701052
      | } """.stripMargin

  val simple = """ 1646701052  """

  case class X(foo: ETimestamp) derives Codec.AsObject

  test("Simple") {
    val res = io.circe.parser.decode[ETimestamp](simple)
    scribe.info(s"${oprint(res)}")
  }

  test("RealCase") {
    val res = io.circe.parser.decodeAccumulating[X](jsonTxt)
    scribe.info(s"${oprint(res)}")
  }
}
