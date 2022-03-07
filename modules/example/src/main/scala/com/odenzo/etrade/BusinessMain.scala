package com.odenzo.etrade
import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.base.IOU
import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.client.api.{AccountsApi, MarketApi}
import com.odenzo.etrade.client.engine.{ETradeClient, ETradeContext}
import com.odenzo.etrade.client.services.Services
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.responses.*
import com.odenzo.etrade.oauth.OAuthSessionData
import io.circe.Decoder.Result
import io.circe.Json
import io.circe.syntax.EncoderOps
import org.http4s.client.Client

import java.time.{Instant, LocalDate}
import scala.util.chaining.scalaUtilChainingOps
object BusinessMain {

  val mtdStart: LocalDate                      = LocalDate.now().withDayOfMonth(1)
  val ytdStart: LocalDate                      = LocalDate.now().withDayOfYear(1)
  val mtdEnd: LocalDate                        = LocalDate.now() // Note this may be off by one due to timezone
  val now: LocalDate                           = LocalDate.now() // Note this may be off by one due to timezone
  val minDate                                  = now.minusYears(2)
  scribe.warn(s"DATES: $minDate  <---> $now")
  def run(eclient: ETradeClient): IO[ExitCode] = QuoteDetail
    .values
    .toList
    .filterNot(_ == QuoteDetail.MF_DETAIL)
    .traverse { detail =>
      for {
        account <- fetchRandomAccount(eclient)
        res     <- eclient.fetchCF[QuoteRs](MarketApi.getEquityQuotesCF(NonEmptyChain("NET", "VWIGX"), details = detail, true))
        _        = scribe.info(s" ${oprint(res)}")
        // _       <- callAccountPagingAPI(account.accountIdKey, eclient)
      } yield ExitCode.Success
    }
    .as(ExitCode.Success)

  /** Calls inidividual Account API, returning model. No Paging */
  def callAccountsAPIs(eclient: ETradeClient): IO[Account] = {
    // Here we DO NOT make eclient implicit so the CF run is called and resolved there (to the same stuff in this example)

    for {
      _           <- IO(scribe.info(s"Calling Account API..."))
      accounts    <- eclient.fetchCF[ListAccountsRs](AccountsApi.listAccountsCF).map(_.accounts)
      myAccount   <- accounts.filter(a => a.accountName === "NickName-2" && a.accountStatus === "ACTIVE").pipe(IOU.exactlyOne("MyAccount"))
      accountIdKey = myAccount.accountIdKey
      _           <- IO(scribe.debug(s"Accounts ${oprint(accounts)}"))
      _           <- IO(scribe.info(s"MyAccount  ${oprint(myAccount)}"))
      // balances    <- eclient.fetchCF[AccountBalanceRs](AccountsApi.accountBalancesCF(accountIdKey))
      //  _            = scribe.info(s"Account Balances: ${oprint(balances)}")
//      portfolio <- eclient.fetchCF[PortfolioRs](AccountsApi.viewPortfolioCF(accountId, lots = true))
//      _          = scribe.info(s"PORTFOLIO: ${oprint(portfolio)}")
//      txns        <- eclient.fetchCF[TransactionListRs](AccountsApi.listTransactionsCF(accountIdKey, mtdStart.some, mtdEnd.some))
//      _            = scribe.info(s"Txns First Page: ${oprint(txns)}")
    } yield myAccount

  }

  /** Calls inidividual Account API, returning model. No Paging */
  def fetchRandomAccount(eclient: ETradeClient): IO[Account] = {
    for {
      _           <- IO(scribe.info(s"Calling Account API..."))
      accounts    <- eclient.fetchCF[ListAccountsRs](AccountsApi.listAccountsCF).map(_.accounts)
      myAccount   <- accounts.headOption.pipe(IOU.required("MyAccount"))
      accountIdKey = myAccount.accountIdKey
    } yield myAccount

  }

  /** Call the two Accounts paging API through predefined pagers */
  def callAccountPagingAPI(accountIdKey: String, eclient: ETradeClient): IO[Unit] =
    given Client[IO]    = eclient.c
    given ETradeContext = eclient.config
    for {
      _    <- IO(scribe.info(s"Txns for $accountIdKey  $minDate   $now --> "))
      txns <- Services.listTransactionsApp(accountIdKey, minDate.some, now.some, 45)
      _     = scribe.info(s"Txns APP Results: ${oprint(txns)}")
    } yield ()

}
