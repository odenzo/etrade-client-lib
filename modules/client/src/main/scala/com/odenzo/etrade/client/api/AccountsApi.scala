package com.odenzo.etrade.client.api

import cats.data.{Chain, Kleisli}
import cats.effect.syntax.all.*
import cats.effect.{IO, Resource}
import cats.syntax.all.*
import com.odenzo.etrade.client.engine.*
import com.odenzo.etrade.models.responses.{AccountBalanceRs, ListAccountsRs, PortfolioRs, TransactionListRs}
import com.odenzo.etrade.models.{MarketSession, PortfolioView, Transaction}
import com.odenzo.etrade.oauth.OAuthSessionData
import com.odenzo.etrade.oauth.OAuthSessionData.Contextual
import io.circe.*
import monocle.*
import monocle.syntax.all.*
import org.http4s.*
import org.http4s.Method.GET
import org.http4s.Uri.*
import org.http4s.client.Client
import org.http4s.headers.*

import java.time.LocalDate

/**
  * # API Request Design This is a way to construct Requests for the service, still pretty low level.
  *   - The signing of these requests it done * whwn they * are * invoked.
  *   - From a deisgn point of view I stick with `IO[Require[IO]]` because more usually it can take a while to contruct a request.
  *   - Also note that making the return type `ETradeCall` will get you a ETraceContext for free and any function defined for that.
  */
object AccountsApi extends APIHelper {

  def listAccountsCF: ETradeCall = {
    Request[IO](GET, baseUri / "v1" / "accounts" / "list", headers = acceptJsonHeaders).pure
  }

  def accountBalancesCF(
      accountIdKey: String,
      accountType: Option[String] = None,
      instType: String = "BROKERAGE"
  ): ETradeCall =
    Request[IO](
      GET,
      (baseUri / "v1" / "accounts" / accountIdKey / "balance")
        .withQueryParam("instType", instType)
        .withQueryParam("realTimeNAV", true)
        .withOptionQueryParam("accountType", accountType),
      headers = acceptJsonHeaders
    ).pure

  /**
    * This will automatically page through and accumulate the results. Start date is limited to 90 days in the past? This has paging yet to
    * be implemented. Need to set an Accept Header on this for the media type (XML, JSON, Excel etc.) Hard coded to JSON for now.
    * @param accountIdKey
    *   Account
    * @param startDate
    *   Not more than three years in past.
    * @param endDate
    *   After start date, and future date non-sensical. I think this will be EST timezone in practice, need to experiment. Might changed to
    *   EST zoned date. FromDa Having a paging FS2 stream somewhere, but first do a collect that sync returns one aggregated answer.
    */
  def listTransactionsCF(
      accountIdKey: String,
      startDate: Option[LocalDate] = None,
      endDate: Option[LocalDate] = None,
      count: Int = 50,
      marker: Option[String] = None
  ): ETradeCall = {
    // The request can return 204 with no content, apparently if no transactions in that range.
    scribe.info(s"Calling List Txn on Account $accountIdKey")
    Request[IO](
      GET,
      (baseUri / "v1" / "accounts" / accountIdKey / "transactions")
        .withOptionQueryParam("startDate", startDate.map(_.format(MMddUUUU)))
        .withOptionQueryParam("endDate", endDate.map(_.format(MMddUUUU)))
        .withQueryParam("count", count.toString)
        .withOptionQueryParam("marker", marker)
    ).addHeader(Accept(MediaType.application.json)).pure
  }

  def listTransactionsDetailCF(accountIdKey: String, transactionId: String, storeId: Option[String]): ETradeCall = {
    Request[IO](
      GET,
      (baseUri / "v1" / "accounts" / accountIdKey / "transactions" / transactionId).withOptionQueryParam("storeId", storeId)
    ).addHeader(Accept(MediaType.application.json)).pure
  }

  def viewPortfolioCF(
      accountIdKey: String,
      lots: Boolean = false,
      view: PortfolioView = PortfolioView.PERFORMANCE,
      totalsRequired: Boolean = true,
      marketSession: MarketSession,
      count: Int = 50,
      pageNumber: Option[String] = None // TransactionId
  ): ETradeCall = {
    Request[IO](
      GET,
      (baseUri / "v1" / "accounts" / accountIdKey / "portfolio")
        .withQueryParam("count", count)
        .withQueryParam("totalsRequired", totalsRequired)
        .withQueryParam("view", view.toString)
        .withQueryParam("lots", lots)
        .withOptionQueryParam("pageNumber", pageNumber),
      headers = acceptJsonHeaders
    ).pure
  }

}
