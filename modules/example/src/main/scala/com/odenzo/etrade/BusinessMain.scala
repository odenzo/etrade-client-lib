package com.odenzo.etrade
import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.base.OPrint.oprint
import com.odenzo.etrade.base.IOU
import com.odenzo.etrade.api.requests.*
import com.odenzo.etrade.api.services.Services
import com.odenzo.etrade.apisupport.{ETradeContext, ETradeService}
import com.odenzo.etrade.client.models.OAuthSessionData
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.responses.*
import io.circe.Decoder.Result
import io.circe.Json
import io.circe.syntax.EncoderOps
import org.http4s.EntityDecoder
import org.http4s.client.Client

import java.time.{Instant, LocalDate}
import scala.util.chaining.scalaUtilChainingOps
object BusinessMain {

  val mtdStart: LocalDate                      = LocalDate.now().withDayOfMonth(1)
  val ytdStart: LocalDate                      = LocalDate.now().withDayOfYear(1)
  val mtdEnd: LocalDate                        = LocalDate.now() // Note this may be off by one due to timezone
  val now: LocalDate                           = LocalDate.now() // Note this may be off by one due to timezone
  val minDate                                  = now.minusYears(2)

  def run(client: Client[IO], ctx: ETradeContext): IO[ExitCode] =
      given Client[IO] = client
      given ETradeContext = ctx
      for {
         _       <- badCalls()
      } yield ExitCode.Success
    .as(ExitCode.Success)

  /** Calls inidividual Account API, then allows you to ue client to do the actual call. semiManual Approach
   * Basically, using the raw ClientpIO] power. Note that the Services automate some of the error handline and paging.
   * This just shows how you can still accesss as based level.*/
  def lowLevel(): ETradeService[Account] = {
    // The context function gives us unnamed. I guess we can using it, but summon is nice I think.
    import     org.http4s.circe.jsonOf
    given entityDecoder: EntityDecoder[IO, ListAccountsRs] = jsonOf[IO,ListAccountsRs]
    val client: Client[IO] = summon[Client[IO]]
    for {
      _           <- IO(scribe.info(s"Calling Account API..."))
      accounts    <- client.expect[ListAccountsRs](AccountsApi.listAccountsCF).map(_.accounts)
      firstAcct = accounts.head
    } yield firstAcct

  }

  /** Calls inidividual Account API, returning model. No Paging */
  def firstAccount(): ETradeService[Account] = {
    for {
      myAccount    <- Services.listAccountsApp().map(_.accounts.headOption) >>= IOU.required("First Account is My Account")
      _ = scribe.info(s"Account: ${oprint(myAccount)}")
    } yield myAccount

  }


  /** Quick experiment to see if messages shows up in any other calls besides QuoteRs */
  def badCalls(): ETradeService[Account] = {

    for {
      myAccount   <-firstAccount()
      accountIdKey = myAccount.accountIdKey
//      balances <- Services.accountBalanceApp(accountIdKey,None,"BROKERAGE")
//         _ = scribe.info(s"Balances: ${oprint(balances)}")
//      txn <- Services.listTransactionsApp(accountIdKey,LocalDate.of(2021,10,1).some, LocalDate.of(2021,12,31).some)
//       _ = scribe.info(s" Txn: ${oprint(txn)}")
      //portfolio <- Services.viewPortfolioApp(accountIdKey,false,PortfolioView.FUNDAMENTAL, totals = true)
      cloadflare <- Services.lookUpProductApp("CloudFlare").flatTap(s=>IO(scribe.info(s"Lookup: ${oprint(s)}")))

      intel <- Services.equityQuotesApp(NonEmptyChain.one("VWIGX")).flatTap(s=>IO(scribe.info(s"Qute Index: ${oprint(s)}")))
    } yield myAccount
  }


}
