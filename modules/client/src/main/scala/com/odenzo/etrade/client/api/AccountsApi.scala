package com.odenzo.etrade.client.api

import cats.effect.IO
import com.odenzo.etrade.client.engine.APIHelper
import com.odenzo.etrade.models.responses.{AccountBalanceRs, ListAccountsRs, PortfolioRs}
import com.odenzo.etrade.oauth.OAuthSessionData
import io.circe.*
import org.http4s.{Request, *}
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.Uri.*

import java.time.LocalDate

/** The Requests will all have authentication added and the requets signed before invoking */
object AccountsApi extends APIHelper {

  import com.odenzo.etrade.oauth.OAuthSessionData.*
  def accountBase(base: Uri): Uri = base / "v1" / "accounts"

  // Since only implicits we can make a context function if needed now.
  def listAccounts(using ctx: OAuthSessionData): Request[IO] = {
    Request[IO](GET, baseUri / "v1" / "accounts" / "list")
  }

  def listAccountsCF: Contextual[Request[IO]] = {
    Request[IO](GET, baseUri / "v1" / "accounts" / "list")
  }

  // So, we can curry to make a context function eh! Nope, not exactly but OK
  // What does this buy us, writing programs WITH LAZY BINDING (call site) of the implicit
  // instead of at creation site. Bind when we do the actuall remote call, instead of when we "create" the request
  def accountBalances(
      accountIdKey: String,
      instType: String = "BROKERAGE"
  )(using ctx: OAuthSessionData): Request[IO] = {
    Request[IO](
      GET,
      (baseUri / "v1" / accountIdKey / "balance")
        .withQueryParam("instType", instType)
        .withQueryParam("realTimeNAV", true)
    )

//    fetch[AccountBalanceRs](session.sign(rq))
  }

  def accountBalancesCF(
      accountIdKey: String,
      instType: String = "BROKERAGE"
  ): Contextual[Request[IO]] =
    Request[IO](
      GET,
      (baseUri / "v1" / accountIdKey / "balance")
        .withQueryParam("instType", instType)
        .withQueryParam("realTimeNAV", true)
    )

    //    fetch[AccountBalanceRs](session.sign(rq))

//  /** This will automatically page through and accumulate the results */
//  def listTransactions(accountIdKey: String, startDate: Option[LocalDate] = None, endDate: Option[LocalDate] = None)(
//      implicit session: ETradeSession,
//      c: Client[IO]
//  ): IO[Json] = {
//    // The request can return 204 with no content, apparently if no transactions in that range.
//    scribe.info(s"Calling List Txn on Account $accountIdKey")
//    val count = 50
//    val uri   = (accountBase(session.config.baseUri) / accountIdKey / "transactions")
//      .withOptionQueryParam("startDate", startDate.map(_.format(MMddUUUU)))
//      .withOptionQueryParam("endDate", endDate.map(_.format(MMddUUUU)))
//      .withQueryParam("count", count.toString)
//
//    val rq = Request[IO](GET, uri)
//    fetch[Json](session.sign(rq))
//  }
//
//  def viewPortfolio(accountIdKey: String, lots: Boolean = false, view: String = "QUICK")(
//      implicit session: ETradeSession,
//      c: Client[IO]
//  ): IO[PortfolioRs] = {
//    val uri = (accountBase(session.config.baseUri) / accountIdKey / "portfolio").withQueryParam("count", "200")
//    val rq  = Request[IO](GET, uri)
//    fetch[PortfolioRs](session.sign(rq))
//  }

}
