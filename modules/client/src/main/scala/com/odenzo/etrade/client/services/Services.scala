package com.odenzo.etrade.client.services
import cats.*
import cats.data.{Chain, NonEmptyChain}
import cats.syntax.all.*
import cats.effect.{IO, Resource}
import com.odenzo.etrade.client.api.AccountsApi.{accountBalancesCF, listTransactionsCF, standardCall, viewPortfolioCF}
import com.odenzo.etrade.client.api.MarketApi
import com.odenzo.etrade.models.responses.{AccountBalanceRs, PortfolioRs, TransactionListRs}
import org.http4s.{Request, Response}
import org.http4s.client.Client
import com.odenzo.etrade.client.engine.*
import com.odenzo.etrade.models.Transaction
import io.circe.Decoder

import java.time.LocalDate

object Services extends ServiceHelpers {
  def accountBalanceApp(
      accountIdKey: String,
      accountType: Option[String] = None,
      instType: String = "BROKERAGE"
  ): ETradeService[AccountBalanceRs] = {
    val client = summon[Client[IO]]
    standard[AccountBalanceRs](accountBalancesCF(accountIdKey, accountType, instType))
  }

  /** Gets txns in range, automatically paging through and returning aggregated results */
  def listTransactionsApp(
      accountIdKey: String,
      startDate: Option[LocalDate] = None,
      endDate: Option[LocalDate] = None
  )(using c: Client[IO]): ETradeService[Chain[Transaction]] = {
    // given c: Client[IO]                                = summon[Client[IO]]
    val rqFn: Option[String] => IO[Request[IO]]        = listTransactionsCF(accountIdKey, startDate, endDate, 10, _)
    val extractor: TransactionListRs => Option[String] = (rs: TransactionListRs) => rs.transactionListResponse.marker
    scribe.warn(s"About to call looking function")
    loopingFunction(rqFn, extractor)(None, Chain.empty).map { (responses: Chain[TransactionListRs]) =>
      responses.flatMap(rs => rs.transactionListResponse.transaction)
    }

  }

  def viewPortfolioApp(
      accountIdKey: String,
      lots: Boolean = false,
      view: String = "COMPLETE",
      totals: Boolean = true
  ): ETradeService[Chain[PortfolioRs]] = {
    val rqFn: Option[String] => IO[Request[IO]]  = viewPortfolioCF(accountIdKey, false, "COMPLETE", true, 10, _)
    val extractor: PortfolioRs => Option[String] = (rs: PortfolioRs) => rs.AccountPortfolio.headOption.map(_.next)
    scribe.warn(s"About to call looking function")
    loopingFunction(rqFn, extractor)(None, Chain.empty).map { (responses: Chain[PortfolioRs]) => responses }

  }

  def equityQuotesApp(symbols: NonEmptyChain[String]): ETradeService[Unit] =
    given c: Client[IO] = summon[Client[IO]]
    standard[Unit](MarketApi.getEquityQuotesCF(symbols))

}
