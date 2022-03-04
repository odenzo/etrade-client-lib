package com.odenzo.etrade.client.api

import cats.data.Kleisli
import cats.effect.IO
import com.odenzo.etrade.client.engine.APIHelper
import com.odenzo.etrade.models.responses.{AccountBalanceRs, ListAccountsRs, PortfolioRs}
import com.odenzo.etrade.oauth.OAuthSessionData
import com.odenzo.etrade.oauth.OAuthSessionData.Contextual
import io.circe.*
import org.http4s.{Request, *}
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.Uri.*

import java.time.LocalDate

/**
  * The Requests will all have authentication added and the requets signed before invoking ## THIS IS DEFINATELY AN IMPLEMENTATION WORK IN
  * PROGRESS
  */
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
      (baseUri / "v1" / "accounts" / accountIdKey / "balance").withQueryParam("instType", instType).withQueryParam("realTimeNAV", true)
    )

  }

  // Could add client to contextual function, typedef the whole thing to RestCall[AccountBalanceRs]
  def accountBalancesK(
      accountIdKey: String,
      instType: String = "BROKERAGE"
  )(using client: Client[IO]): Contextual[Kleisli[IO, Request[IO], AccountBalanceRs]] =
    import org.http4s.circe.CirceEntityDecoder.*
    val rq = Request[IO](
      GET,
      (baseUri / "v1" / accountIdKey / "balance").withQueryParam("instType", instType).withQueryParam("realTimeNAV", true)
    )

    client.toKleisli(rs => rs.as[AccountBalanceRs])

  def accountBalancesCF(accountIdKey: String, accountType: Option[String] = None, instType: String = "BROKERAGE"): Contextual[Request[IO]] =
    Request[IO](
      GET,
      (baseUri / "v1" / "accounts" / accountIdKey / "balance")
        .withQueryParam("instType", instType)
        .withQueryParam("realTimeNAV", true)
        .withOptionQueryParam("accountType", accountType)
    )

  //    fetch[AccountBalanceRs](session.sign(rq))

  /**
    * This will automatically page through and accumulate the results. Start date is limited to 90 days in the past? This has paging yet to
    * be implemented
    * @param accountIdKey
    *   Account
    * @param startDate
    *   Not more than 90 in the past
    * @param endDate
    *   After start date, and future date non-sensical. I think this will be EST timezone in practice, need to experiment. Might changed to
    *   EST zoned date.
    */
  def listTransactionsCF(
      accountIdKey: String,
      startDate: Option[LocalDate] = None,
      endDate: Option[LocalDate] = None
  ): Contextual[Request[IO]] = {
    // The request can return 204 with no content, apparently if no transactions in that range.
    scribe.info(s"Calling List Txn on Account $accountIdKey")
    val count = 50 // Meh...a layer above for business level scrolling. Or is their streaming alternative?
    Request[IO](
      GET,
      (baseUri / "v1" / accountIdKey / "transactions")
        .withOptionQueryParam("startDate", startDate.map(_.format(MMddUUUU)))
        .withOptionQueryParam("endDate", endDate.map(_.format(MMddUUUU)))
        .withQueryParam("count", count.toString)
    )

  }

  def viewPortfolioCF(accountIdKey: String, lots: Boolean = false, view: String = "QUICK"): Contextual[Request[IO]] = {
    Request[IO](GET, (baseUri / "v1" / accountIdKey / "portfolio").withQueryParam("count", "200"))
  }

}
