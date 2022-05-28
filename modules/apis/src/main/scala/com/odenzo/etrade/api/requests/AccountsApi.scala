package com.odenzo.etrade.api.requests

import cats.*
import cats.data.*
import cats.effect.syntax.all.*
import cats.effect.*
import cats.syntax.all.*
import com.odenzo.etrade.models.responses.*
import com.odenzo.etrade.models.*
import com.odenzo.etrade.api.*

import io.circe.*
import monocle.*
import monocle.syntax.all.*
import org.http4s.*
import org.http4s.Method.GET
import org.http4s.Uri.*
import org.http4s.client.Client
import org.http4s.headers.*

import java.time.LocalDate
import scala.language.postfixOps

/**
  * # API Request Design This is a way to construct Requests for the service, still pretty low level.
  *   - The signing of these requests it done * whwn they * are * invoked.
  *   - From a deisgn point of view I stick with `IO[Require[IO]]` because more usually it can take a while to contruct a request.
  *   - Also note that making the return type `ETradeCall` will get you a ETraceContext for free and any function defined for that.
  */
object AccountsApi extends APIHelper {

  def listAccountsCF(): ETradeCall = {
    Request[IO](GET, baseUri / "v1" / "accounts" / "list", headers = acceptJsonHeaders).pure
  }

  def listAccountsApp(): ETradeService[List[Account]] = standard[ListAccountsRs](listAccountsCF()).map(_.accounts)

  def accountBalancesCF(
      accountIdKey: String,
      accountType: Option[String],
      instType: String
  ): ETradeCall =
    val realTimeNAV: Boolean = true
    Request[IO](
      GET,
      (baseUri / "v1" / "accounts" / accountIdKey / "balance")
        .withQueryParam("instType", instType)
        .withQueryParam("realTimeNAV", realTimeNAV)
        .withOptionQueryParam("accountType", accountType),
      headers = acceptJsonHeaders
    ).pure

  /** Get the Account Balances */
  def accountBalanceApp(
      accountIdKey: String,
      accountType: Option[String],
      instType: String
  ): ETradeService[AccountBalanceRs] = standard[AccountBalanceRs](accountBalancesCF(accountIdKey, accountType, instType))

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

  /** Gets txns in range, automatically paging through and returning aggregated results. 4xs gives me XML error */
  def listTransactionsApp(
      accountIdKey: String,
      startDate: Option[LocalDate],
      endDate: Option[LocalDate],
      count: Int
  ): ETradeService[Chain[Transaction]] = {
    import com.odenzo.etrade.models.*
    val rqFn: Option[String] => IO[Request[IO]]        = listTransactionsCF(accountIdKey, startDate, endDate, count, _)
    val extractor: TransactionListRs => Option[String] = (rs: TransactionListRs) => rs.transactionListResponse.marker
    iteratePages(rqFn, extractor)(None, Chain.empty).map {
      (responses: Chain[TransactionListRs]) => responses.flatMap(rs => rs.transactionListResponse.transaction)
    }
  }

  /** This endpoint is overloaded a bit much, and response format is too. This can be paging. */
  def transactionsDetailCF(accountIdKey: String, transactionId: String, storeId: Option[StoreId]): ETradeCall = {
    Request[IO](
      GET,
      (baseUri / "v1" / "accounts" / accountIdKey / "transactions" / transactionId).withOptionQueryParam("storeId", storeId)
    ).addHeader(Accept(MediaType.application.json)).pure
  }

  def transactionDetailsApp(
      accountIdKey: String,
      txnId: String,
      storeId: Option[StoreId],
      cat: Option[TransactionCategory]
  ): ETradeService[TransactionDetailsRs] = standard[TransactionDetailsRs](transactionsDetailCF(accountIdKey, txnId, storeId))

  def viewPortfolioCF(
      accountIdKey: String,
      lots: Boolean = false,
      view: PortfolioView = PortfolioView.PERFORMANCE,
      totalsRequired: Boolean = true,
      marketSession: MarketSession,     // FIXME: Not currently used
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

  def viewPortfolioApp(
      accountIdKey: String,
      lots: Boolean = false,
      view: PortfolioView = PortfolioView.PERFORMANCE,
      totals: Boolean = true,
      marketSession: MarketSession = MarketSession.REGULAR,
      count: Int
  ): ETradeService[Chain[ViewPortfolioRs]] = {
    val rqFn: Option[String] => IO[Request[IO]]      = viewPortfolioCF(accountIdKey, lots, view, totals, marketSession, count, _)
    val extractor: ViewPortfolioRs => Option[String] = (rs: ViewPortfolioRs) => rs.accountPortfolio.head.nextPageNo
    scribe.warn(s"About to call looking function")

    iteratePages(rqFn, extractor)(None, Chain.empty).map {
      (responses: Chain[ViewPortfolioRs]) => responses
    }

  }

  case class FullPortfolio(totals: Option[PortfolioTotals], portfolio: List[AccountPortfolio]),
}
