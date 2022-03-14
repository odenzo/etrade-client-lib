package com.odenzo.etrade.models

import munit.*
import io.circe.*
import io.circe.syntax.*
class PortfolioViewTest extends FunSuite with munit.Assertions {

  test("PortfolioViewCodec") {

    assertEquals(PortfolioView.PERFORMANCE.asJson, Json.fromString("PERFORMANCE"))
    assertEquals(Json.fromString("PERFORMANCE").as[PortfolioView], Right(PortfolioView.PERFORMANCE))
    assertEquals(Json.fromString("performance").as[PortfolioView], Right(PortfolioView.PERFORMANCE))
  }
}
