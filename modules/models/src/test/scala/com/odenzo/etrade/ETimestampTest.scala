package com.odenzo.etrade.models

import com.odenzo.base.OPrint.oprint
import io.circe.Decoder.Result
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Json, JsonObject, ParsingFailure}
import munit.FunSuite

import javax.tools.ForwardingFileObject

class ETimestampTest extends FunSuite {

  val jsonTxt: String =
    """{
      |  "x" : 1646701052,
      |    "QuoteData" :
      |      {
      |        "dateTime" : "19:57:32 EST 03-07-2022",
      |        "dateTimeUTC" : 1646701052,
      |      }
      | } """.stripMargin

  val simple = """ 1646701052  """

  test("Simple") {
    val res = io.circe.parser.decodeAccumulating[ETimestamp](simple)
    scribe.info(s"${oprint(res)}")
  }
}
