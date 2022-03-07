package com.odenzo.etrade.client.api

import cats.data.{Chain, Kleisli}
import cats.effect.{IO, Resource}
import com.odenzo.etrade.client.api.AccountsApi.standardCall
import com.odenzo.etrade.client.engine.{APIHelper, ETradeContext}
import com.odenzo.etrade.models.Transaction
import com.odenzo.etrade.models.responses.{AccountBalanceRs, ListAccountsRs, PortfolioRs, TransactionListRs}
import com.odenzo.etrade.oauth.OAuthSessionData
import com.odenzo.etrade.oauth.OAuthSessionData.Contextual
import io.circe.*
import org.http4s.{Request, *}
import org.http4s.Method.GET
import org.http4s.client.Client
import org.http4s.Uri.*
import org.http4s.headers.Accept
import monocle.*
import monocle.syntax.all.*
import com.odenzo.etrade.client.engine.*
import java.time.LocalDate

/**
  * The Requests will all have authentication added and the requets signed before invoking ## THIS IS DEFINATELY AN IMPLEMENTATION WORK IN
  * PROGRESS
  */
object AccountsApi extends APIHelper {

  def listAccountsCF: ETradeCall = {
    Request[IO](GET, baseUri / "v1" / "accounts" / "list")
  }

  def accountBalancesCF(
      accountIdKey: String,
      accountType: Option[String] = None,
      instType: String = "BROKERAGE"
  ): ETradeCall = Request[IO](
    GET,
    (baseUri / "v1" / "accounts" / accountIdKey / "balance")
      .withQueryParam("instType", instType)
      .withQueryParam("realTimeNAV", true)
      .withOptionQueryParam("accountType", accountType)
  )

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
      marker: Option[String] = None // TransactionId
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
    ).addHeader(Accept(MediaType.application.json))

  }

  def viewPortfolioCF(
      accountIdKey: String,
      lots: Boolean = false,
      view: String = "COMPLETE",
      totals: Boolean = true,
      count: Int = 50
  ): ETradeCall = {
    Request[IO](GET, (baseUri / "v1" / accountIdKey / "portfolio").withQueryParam("count", count))
  }

}
