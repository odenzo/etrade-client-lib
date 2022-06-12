package com.odenzo.etrade.models

import io.circe.Decoder.Result
import munit.FunSuite

class EnumsTest extends FunSuite {

  import io.circe.*
  import io.circe.syntax.*
  import com.odenzo.etrade.models.*

  test("CIEnum-MarginLevel") {
    val o       = MarginLevel.MARGIN_TRADING_ALLOWED_ON_PM
    val s       = o.asJson
    scribe.info(s"Color: $o => ${s.spaces4}")
    val bo      = s.as[MarginLevel]
    assertEquals(bo, Right(o))
    scribe.info(s"$bo")
    val origStr = o.toString
    val lcStr   = origStr.toLowerCase
    val bo2     = Json.fromString(lcStr).as[MarginLevel]
    assertEquals(bo2, Right(o))
  }
}
