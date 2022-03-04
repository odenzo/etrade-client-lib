package com.odenzo.etrade
import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.base.IOU
import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.client.api.AccountsApi
import com.odenzo.etrade.client.engine.ETradeClient
import com.odenzo.etrade.models.Account
import com.odenzo.etrade.models.responses.{AccountBalanceRs, ListAccountsRs, ListTransactionsRs, PortfolioRs}
import com.odenzo.etrade.oauth.OAuthSessionData
import io.circe.Decoder.Result
import io.circe.Json
import io.circe.syntax.EncoderOps
import org.http4s.client.Client

import java.time.LocalDate
import scala.util.chaining.scalaUtilChainingOps
object BusinessMain {

  val mtdStart: LocalDate = LocalDate.now().withDayOfMonth(1)
  val mtdEnd: LocalDate   = LocalDate.now() // Note this may be off by one due to timezone

  def run()(using eclient: ETradeClient): IO[ExitCode] = {
    given OAuthSessionData = eclient.session
    callAccountsAPIs(eclient).as(ExitCode.Success)
  }

  /** Returns accountId which wil */
  def callAccountsAPIs(eclient: ETradeClient): IO[Account] = {
    // Here we DO NOT make eclient implicit so the CF run is called and resolved there (to the same stuff in this example)

    for {
      _           <- IO(scribe.info(s"BusinessMain Running..."))
      accounts    <- eclient.fetchCF[ListAccountsRs](AccountsApi.listAccountsCF).map(_.accounts)
      myAccount   <- accounts.filter(a => a.accountName === "NickName-2" && a.accountStatus === "ACTIVE").pipe(IOU.exactlyOne("MyAccount"))
      accountIdKey = myAccount.accountIdKey
      _           <- IO(scribe.debug(s"Accounts ${oprint(accounts)}"))
      _           <- IO(scribe.info(s"MyAccount  ${oprint(myAccount)}"))
      balances    <- eclient.fetchCF[AccountBalanceRs](AccountsApi.accountBalancesCF(accountIdKey))
      _            = scribe.info(s"Account Balances: ${oprint(balances)}")
//      portfolio <- eclient.fetchCF[PortfolioRs](AccountsApi.viewPortfolioCF(accountId, lots = true))
//      _          = scribe.info(s"PORTFOLIO: ${oprint(portfolio)}")
//      txns      <- eclient.fetchCF[ListTransactionsRs](AccountsApi.listTransactionsCF(accountId, mtdStart.some, mtdEnd.some))
    } yield myAccount

  }
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
