package com.odenzo.etrade.client.services
import cats.*
import cats.data.{Chain, NonEmptyChain}
import cats.syntax.all.*
import cats.effect.{IO, Resource}
import com.odenzo.etrade.client.api.AccountsApi.*
import com.odenzo.etrade.client.api.MarketApi
import com.odenzo.etrade.client.api.MarketApi.*
import com.odenzo.etrade.models.responses.*
import org.http4s.{Request, Response}
import org.http4s.client.Client
import com.odenzo.etrade.client.engine.*
import com.odenzo.etrade.models.{MarketSession, PortfolioView, Transaction}
import io.circe.Decoder
import org.http4s.Method.GET

import java.time.LocalDate

object Services extends ServiceHelpers {

  def listAccountsApp(): ETradeService[ListAccountsRs] = {
    val client = summon[Client[IO]]
    standard[ListAccountsRs](listAccountsCF)
  }

  /** Get the Account Balances */
  def accountBalanceApp(
      accountIdKey: String,
      accountType: Option[String] = None,
      instType: String = "BROKERAGE"
  ): ETradeService[AccountBalanceRs] = {
    val client = summon[Client[IO]]
    standard[AccountBalanceRs](accountBalancesCF(accountIdKey, accountType, instType))
  }

  /** Gets txns in range, automatically paging through and returning aggregated results. 4xs gives me XML error */
  def listTransactionsApp(
      accountIdKey: String,
      startDate: Option[LocalDate] = None,
      endDate: Option[LocalDate] = None
  )(using c: Client[IO]): ETradeService[Chain[Transaction]] = {
    import com.odenzo.etrade.models.*
    val rqFn: Option[String] => IO[Request[IO]]        = listTransactionsCF(accountIdKey, startDate, endDate, 10, _)
    val extractor: TransactionListRs => Option[String] = (rs: TransactionListRs) => rs.transactionListResponse.marker
    loopingFunction(rqFn, extractor)(None, Chain.empty).map { (responses: Chain[TransactionListRs]) =>
      responses.flatMap(rs => rs.transactionListResponse.transaction)
    }

  }

  def viewPortfolioApp(
      accountIdKey: String,
      lots: Boolean = false,
      view: PortfolioView = PortfolioView.PERFORMANCE,
      totals: Boolean = true,
      marketSession: MarketSession = MarketSession.REGULAR
  ): ETradeService[Chain[PortfolioRs]] = {
    val rqFn: Option[String] => IO[Request[IO]]  = viewPortfolioCF(accountIdKey, lots, view, totals, marketSession, 2, _)
    val extractor: PortfolioRs => Option[String] = (rs: PortfolioRs) => rs.AccountPortfolio.head.nextPageNo
    scribe.warn(s"About to call looking function")
    loopingFunction(rqFn, extractor)(None, Chain.empty).map { (responses: Chain[PortfolioRs]) => responses }

  }

  def equityQuotesApp(symbols: NonEmptyChain[String]): ETradeService[Unit] =
    given c: Client[IO] = summon[Client[IO]]
    standard[Unit](MarketApi.getEquityQuotesCF(symbols))

}
