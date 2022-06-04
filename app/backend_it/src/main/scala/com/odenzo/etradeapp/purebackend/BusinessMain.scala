package com.odenzo.etradeapp.purebackend

import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import cats.effect.syntax.all.*
import com.odenzo.etrade.api.ETradeContext
import com.odenzo.etrade.api.commands.{*, given}
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.responses.*
import com.odenzo.etrade.oauth.server.*
import com.odenzo.etrade.oauth.{ClientOAuth, OAuthClientMiddleware}
import org.http4s.client.Client
import com.odenzo.etrade.api.commands.{CommandRunner, given}
import com.odenzo.etrade.api.requests.{
  AccountBalancesCmd,
  EquityQuoteCmd,
  ListAccountsCmd,
  ListTransactionsCmd,
  LookupProductCmd,
  TransactionDetailsCmd,
  ViewPortfolioCmd
}
import io.circe.syntax.{*, given}

import java.time.LocalDate
import scala.annotation.unused

/**
  * A Pure Backend App that uses etrade-client-lib to do login (and popup browser) Just a quick have to demonstrate logging in and pulling
  * some info. This has the default mode to load an access token from disk. Check if it works, if not, create a new one and override the one
  * on disk. Meh.... some autologin browser scraping thing better but I am sure its not straight forward.
  */
object BusinessMain {

  // Given a use function, fn:A => F[B]
  // Normal use resource.use (fn)
  // I want to say resource.use AsGivens(fn)
  // So, instead of written I need to convert fn to fn(using A)
  // def asGiven[]
  // Hmm can I make a function  bridge[T](t:T)(f:=>IO[Unit]): IO[Unit] = { given T = t ; f)
  // With useage as  resource.use bridge { summon[T]... meh, its nested scope. if I invoke f its
  // won't have t in scope, but with an inline macro?

  def business(resource: Resource[IO, (Client[IO], ETradeContext)]): IO[Unit] = {
    resource
      .use { (client: Client[IO], context: ETradeContext) =>
        given ETradeContext = context
        given Client[IO]    = client
        incremental()
      }
      .onError {
        err =>
          IO(scribe.info(s"Logging in in Business Main:", err)) *>
            IO.trace.flatMap(trace => IO(scribe.error(s"BUSINESS MAIN TRACE:\n ${trace.pretty}")))
      }

  }
  def incremental()(using Client[IO], ETradeContext): IO[Unit] = {

    for {
      accountsRs <- ListAccountsCmd().exec()
      _           = scribe.info(s"Accounts: ${pprint(accountsRs)}")
      _          <- IO.fromEither(accountsRs.asJson.as[ListAccountsRs])
      account     = accountsRs.accounts.head
      accountId   = account.accountIdKey
      _           = scribe.info(s"Your Account: ${pprint(account)}")
      balancesRs <- AccountBalancesCmd(accountId, account.accountType.some, account.institutionType).exec()
      _          <- IO.fromEither(balancesRs.asJson.as[com.odenzo.etrade.models.responses.AccountBalanceRs])
      _           = scribe.info(s"Balances: ${pprint(balancesRs)}")
      listTxnsRs <- ListTransactionsCmd(accountId, LocalDate.of(2021, 1, 1).some, LocalDate.of(2022, 5, 25).some, 45).exec()
      _           = scribe.info(s"Txns1: ${listTxnsRs.transactions.size} ${listTxnsRs}")
      _          <- IO.fromEither(listTxnsRs.asJson.as[com.odenzo.etrade.models.responses.ListTransactionsRs])
      txnByType   = listTxnsRs.transactions.toList.groupBy(_.transactionType)
      _           = scribe.info(s"Unique Transaction Types: ${pprint(txnByType.keys)}")
      oneOfEach   = txnByType.values.map(_.head).toList
      _           = scribe.info(s"OneOfEach ${pprint(oneOfEach)}")
      detailsRs  <- oneOfEach.traverse { txn => TransactionDetailsCmd(accountId, txn.transactionId, txn.storeId.some, None).exec() }
      _          <- detailsRs.traverse { rs => IO.fromEither(rs.asJson.as[TransactionDetailsRs]) }
      _           = scribe.info(s"Transaction Details, one per Category ${pprint(detailsRs)}")

      viewPortfolioRs <- ViewPortfolioCmd(
                           accountIdKey = accountId,
                           lots = true,
                           view = PortfolioView.PERFORMANCE,
                           totalsRequired = true,
                           marketSession = MarketSession.REGULAR, // Iterate through these
                           count = 250
                         ).exec()
      _                = scribe.info(s"Basic PortfolioPerformance View: ${pprint(viewPortfolioRs)}")
      _               <- IO.fromEither(viewPortfolioRs.asJson.as[ViewPortfolioRs])
      lookupRs        <- LookupProductCmd("VWI").exec()
      _                = scribe.info(s"Lookup Response: ${pprint(lookupRs)}")
      _               <- IO.fromEither(lookupRs.asJson.as[LookupProductRs])
      quote           <- quote()
    } yield ()
  }

  def tryAllPortfolioViews(account: Account)(using Client[IO], ETradeContext) = {
    val base = ViewPortfolioCmd(
      accountIdKey = account.accountIdKey,
      lots = true,
      view = PortfolioView.PERFORMANCE,
      totalsRequired = true,
      marketSession = MarketSession.EXTENDED, // Iterate through these
      count = 250
    )

    val allViews    = PortfolioView.values
    val allSessions = MarketSession.values
  }

  def quote()(using Client[IO], ETradeContext) = {

    val allDetails  = QuoteDetail.values.toList.filterNot(_ == QuoteDetail.MF_DETAIL)
    val earnings    = List(true, false)
    val miniOptions = List(true, false)
    val cartesion   =
      for {
        details <- allDetails
        earn    <- earnings
        options <- miniOptions
      } yield (details, earn, options)

//
    val cmds = cartesion
      .toList
      .map { case (d, e, o) =>
        EquityQuoteCmd(
          symbols = NonEmptyChain("FUV", "AAPL"),
          details = d,
          requireEarnings = e,
          skipMiniOptionsCheck = o
        )
      }

    val result: IO[List[Any]] = cmds.traverse { cmd =>
      scribe.info(s"CMD: ${pprint(cmd)}")
      cmd.exec().handleError(e => cmd).flatTap(r => IO(scribe.info(s"Results: ${pprint(cmd)} =>\n ${pprint(r)}")))
    }
    result
  }

  val acctId                                                                                                                   = "666"
  val commands: (ListAccountsCmd, AccountBalancesCmd, LookupProductCmd, EquityQuoteCmd, ListTransactionsCmd, ViewPortfolioCmd) =
    (
      ListAccountsCmd(),
      AccountBalancesCmd(acctId, None),
      LookupProductCmd("APPLE"),
      EquityQuoteCmd(symbols = NonEmptyChain("AAPL"), details = QuoteDetail.ALL, true, true),
      ListTransactionsCmd(acctId, LocalDate.of(2022, 1, 1).some, LocalDate.of(2022, 5, 25).some, 45),
      //            TransactionDetailsCmd(acctId, txn.transactionId, None, None) // Need to find a txn first :-/
      ViewPortfolioCmd(
        acctId,
        lots = true,
        view = PortfolioView.PERFORMANCE,
        totalsRequired = true,
        marketSession = MarketSession.REGULAR,
        count = 250
      )
    )

}
