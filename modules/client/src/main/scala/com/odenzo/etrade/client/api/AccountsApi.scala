//package com.odenzo.etrade.client.api
//
//import cats.effect.IO
//import com.odenzo.etrade.models.responses.{AccountBalanceRs, ListAccountsRs, PortfolioRs}
//import io.circe.*
//import org.http4s.*
//import org.http4s.Method.GET
//import org.http4s.client.Client
//
//import java.time.LocalDate
//
//object AccountsApi extends APIHelper with UsingRestClient {
//
//  def accountBase(base: Uri): Uri = base / "v1" / "accounts"
//
//  def listAccounts(implicit session: ETradeSession, c: Client[IO]): IO[ListAccountsRs] = {
//    val uri                     = Request[IO](GET, session.config.baseUri / "v1" / "accounts" / "list")
//    val signed: IO[Request[IO]] = session.sign(uri)
//    fetch[ListAccountsRs](signed)(ListAccountsRs.decoder, c)
//  }
//
//  def accountBalances(
//      accountIdKey: String,
//      instType: String = "BROKERAGE"
//  )(implicit session: ETradeSession, c: Client[IO]): IO[AccountBalanceRs] = {
//
//    val uri = (accountBase(session.config.baseUri) / accountIdKey / "balance")
//      .withQueryParam("instType", instType)
//      .withQueryParam("realTimeNAV", true)
//
//    val rq = Request[IO](GET, uri)
//    fetch[AccountBalanceRs](session.sign(rq))
//  }
//
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
//
//}
