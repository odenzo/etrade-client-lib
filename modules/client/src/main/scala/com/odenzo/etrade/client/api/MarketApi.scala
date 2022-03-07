package com.odenzo.etrade.client.api

import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.IO
import com.odenzo.etrade.client.engine.*

import com.odenzo.etrade.models.*
import org.http4s.Method.GET
import org.http4s.Request

object MarketApi extends APIHelper {

  def getEquityQuotesCF(
      symbols: NonEmptyChain[String],
      details: QuoteDetail = QuoteDetail.INTRADAY,
      requireEarnings: Boolean = false
  ): ETradeCall = {
    val moreSymbols: IO[Boolean] =
      symbols.length match {
        case len if len > 50 => IO.raiseError(Throwable(s"# Symbol must be 50 or less but $len"))
        case len if len > 25 => IO.pure(true)
        case _               => IO.pure(false)
      }

    // val detailFlag = "ALL" // WEEK_52? MF_DETAIL
    val csv = symbols.reduceLeft((a, b) => s"$a,$b")
    for {
      over <- moreSymbols
      rq    = Request[IO](
                GET,
                (baseUri / "v1" / "market" / "quote" / csv)
                  .withQueryParam("overrideSymbolCount", over)
                  .withQueryParam("detailFlag", details.toString)
                  .withQueryParam("requireEarningsDate", requireEarnings)
              )
    } yield rq
  }
}
