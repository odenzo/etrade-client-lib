package com.odenzo.etrade
import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.base.IOU
import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.client.api.AccountsApi
import com.odenzo.etrade.client.engine.{ETradeClient, ETradeContext}
import com.odenzo.etrade.client.services.Services
import com.odenzo.etrade.models.Account
import com.odenzo.etrade.models.responses.{AccountBalanceRs, ListAccountsRs, PortfolioRs, TransactionListResponse, TransactionListRs}
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
  def run(eclient: ETradeClient): IO[ExitCode] = {
    for {
      account <- fetchRandomAccount(eclient)
      _       <- callAccountPagingAPI(account.accountIdKey, eclient)
    } yield ExitCode.Success

  }

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
//  def downloadAllTxn()(using client: Client[IO], login: OAuthSessionData): IO[Unit] = {
//
//    val accountKey: IO[String] = for {
//      _            <- IO(scribe.info(s"Have Access Token Etc and now doing some work..."))
//      accounts     <- AccountsApi.listAccounts
//      //  _            <- IO(scribe.info(s"Accounts ${oprint(accounts)}"))
//      accountIdKey <- accounts.accounts.headOption.map(_.accountIdKey) pipe IOU.required("accountIdKey")
//      //  _             = scribe.info(s"Account Key ID: $accountIdKey")
//      _            <- getMonth(accountIdKey, LocalDate.of(2018, 11, 1))
//    } yield accountIdKey
//    accountKey.void
//  }

//  def getMonth(accountKey: String, start: LocalDate)(implicit s: ETradeSession, c: Client[IO]): IO[Unit] = {
//
//    val end = start.plusMonths(1)
//    if (!start.isBefore(LocalDate.of(2019, 1, 1))) then IO.unit
//    else
//      for {
//        txns <- AccountsApi.listTransactions(accountKey, start.some, end.some)
//        _    <- CirceUtils.writeJson(txns, new java.io.File(s"txns_${start.toString}.json")).to[IO]
//        _     = scribe.info(s"Start $start has More: ${needToPage(txns)}")
//        _    <- getMonth(accountKey, end)
//      } yield ()
//  }

//  def needToPage(json: Json): Boolean = {
//    (json \\ "moreTransactions").headOption match {
//      case None    => throw new Exception("moreTransactions field not found")
//      case Some(h) => h.asBoolean.getOrElse(throw new Exception(s"Invalid JSON for Bool ${h.spaces4}"))
//    }
//  }
}
