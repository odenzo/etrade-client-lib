package com.odenzo.etrade.api.requests

import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import cats.effect.syntax.all.*
import com.odenzo.etrade.models.*
import org.http4s.Method.GET
import org.http4s.Request
import com.odenzo.etrade.api.*

import com.odenzo.etrade.models.responses.{LookupProductRs, QuoteRs}

object MarketApi extends APIHelper {

  def lookUpProductCF(search: String): ETradeCall = IO.pure(Request[IO](
    GET,
    (baseUri / "v1" / "market" / "lookup" / search),
    headers = acceptJsonHeaders
  ))

  def lookUpProductApp(search: String): ETradeService[LookupProductRs] = standard[LookupProductRs](MarketApi.lookUpProductCF(search))

  /**
    * First time I get an error (e.g. bad symbol). Docs are wrong, 200 still returned, which Messages I am not sure if I ever get messages
    * AND the full reponse or not. Could probably punt on either one of them instead of Ior, just to see if QuoteRs | QuoteErrorRs works
    * well enough. But in that case I should raise an ETradeError with messages data.
    */
  def getEquityQuotesCF(
      symbols: NonEmptyChain[String],
      details: QuoteDetail,
      requireEarnings: Boolean,
      skipMiniOptionCheck: Boolean
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
                  .withQueryParam("skipMiniOptionsCheck", requireEarnings)
                  .withQueryParam("requireEarningsDate", requireEarnings),
                headers = acceptJsonHeaders
              )
    } yield rq
  }

  def equityQuotesApp(
      symbols: NonEmptyChain[String],
      details: QuoteDetail,
      withEarnings: Boolean,
      skipMiniOptionCheck: Boolean
  ): ETradeService[QuoteRs] = standard[QuoteRs](MarketApi.getEquityQuotesCF(symbols, details, withEarnings, skipMiniOptionCheck))

}
