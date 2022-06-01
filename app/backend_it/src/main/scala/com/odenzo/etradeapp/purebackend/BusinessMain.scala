package com.odenzo.etradeapp.purebackend

import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import cats.effect.syntax.all.*
import com.odenzo.etrade.api.ETradeContext
import com.odenzo.etrade.api.commands.{*, given}
import com.odenzo.etrade.api.models.*
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.responses.*
import com.odenzo.etrade.oauth.server.*
import com.odenzo.etrade.oauth.{ClientOAuth, OAuthClientMiddleware}
import org.http4s.client.Client
import com.odenzo.etrade.api.commands.{CommandRunner, given}

import java.time.LocalDate
import scala.annotation.unused

/**
  * A Pure Backend App that uses etrade-client-lib to do login (and popup browser) Just a quick have to demonstrate logging in and pulling
  * some info. This has the default mode to load an access token from disk. Check if it works, if not, create a new one and override the one
  * on disk. Meh.... some autologin browser scraping thing better but I am sure its not straight forward.
  */
object BusinessMain {

  def business(resource: Resource[IO, (Client[IO], ETradeContext)]): IO[Unit] = {
    resource.use { (client, context) =>
      given ETradeContext = context

      given Client[IO] = client
      incremental()
    }
  }
  def incremental()(using Client[IO], ETradeContext): IO[Unit]                = {

    for {
      accountsRs <- ListAccountsCmd().exec()
      _           = scribe.info(s"Accounts: ${pprint(accountsRs)}")
      account     = accountsRs.accounts.head
      accountId   = account.accountIdKey
      _           = scribe.info(s"Your Account: ${pprint(account)}")
      balances   <- AccountBalancesCmd(accountId, account.accountType.some, account.institutionType).exec()
      _           = scribe.info(s"Balances: ${pprint(balances)}")

      // BOTH START AND END or IGNORED
      txn1     <- ListTransactionsCmd(accountId, LocalDate.of(2021, 1, 1).some, LocalDate.of(2022, 5, 25).some, 45).exec()
      _         = scribe.info(s"Txns1: ${txn1.transactions.size} ${txn1}")
      txnByType = txn1.transactions.toList.groupBy(_.transactionType)
      _         = scribe.info(s"Unique Transaction Types: ${pprint(txnByType.keys)}")
      oneOfEach = txnByType.values.map(_.head).toList
      _         = scribe.info(s"OneOfEach ${pprint(oneOfEach)}")
      details  <- oneOfEach.traverse(txn => TransactionDetailsCmd(accountId, txn.transactionId, txn.storeId.some, None).exec())
      _         = scribe.info(s"Transaction Details, one per Category ${pprint(details)}")

      portfolioView <- ViewPortfolioCmd(
                         accountIdKey = accountId,
                         lots = true,
                         view = PortfolioView.PERFORMANCE,
                         totalsRequired = true,
                         marketSession = MarketSession.REGULAR, // Iterate through these
                         count = 250
                       ).exec()
      _              = scribe.info(s"Basic PortfolioPerformance View: ${pprint(portfolioView)}")

      lookup <- LookupProductCmd("VWI").exec()
      _       = scribe.info(s"Lookup Response: ${pprint(lookup)}")
      quote  <- quote()
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
