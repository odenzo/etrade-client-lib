package com.odenzo.etrade.api.requests

import cats.*
import cats.data.*
import cats.effect.syntax.all.*
import cats.effect.*
import cats.syntax.all.*
import com.odenzo.etrade.models.responses.{AccountBalanceRs, ListAccountsRs, PortfolioRs, TransactionListRs}
import com.odenzo.etrade.models.{MarketSession, PortfolioView, Transaction}
import com.odenzo.etrade.api.*
import com.odenzo.etrade.api.ETradeContext.*
import com.odenzo.etrade.api.utils.APIHelper
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

  def transactionsDetailCF(accountIdKey: String, transactionId: String, storeId: Option[String]): ETradeCall = {
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

  // Hmm, composing (a,b,..) => IO[T] with IO[T] => IO[U] to yield (a,b...) => IO[U] should be easy?
  // Scala compose  / andThen only works on Function1
  case class Foo(something: String)
  // given Foo                                               = Foo("implicitness")
  def testT(a: Int, b: Int)(using x: Foo): IO[BigDecimal] = IO.pure(BigDecimal(a * b))
  def testU(t: IO[BigDecimal]): IO[String]                = t.map(v => s"T was $v")

  val fnTestT: (Int, Int) => IO[BigDecimal]          = {
    given Foo = Foo("implicit")
    testT _
  }
  val fnTestU: IO[BigDecimal] => IO[Fragment]        = testU _
  val reqKleise: ReaderT[IO, (Int, Int), BigDecimal] = Kleisli(fnTestT.tupled)

  import cats.arrow.{given, *}

  val serviceA: ((Int, Int)) => IO[Fragment] = fnTestT.tupled.andThen(fnTestU)

  val service: ((Int, Int)) => IO[Fragment] = fnTestU.compose(fnTestT.tupled)

  // So we end up with a A => F[Result] but with initial params tupled.

  // val testMe: (Fragment, Boolean, PortfolioView, Boolean, MarketSession, Int, Option[Fragment]) =>
  // IO[Request[IO]] = (viewPortfolioCF _)
}