package com.odenzo.etradeapp.purebackend

import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import cats.effect.syntax.all.*
import com.odenzo.etrade.api.ETradeContext
import com.odenzo.etrade.api.commands.{*, given}
import com.odenzo.etrade.api.models.*
import com.odenzo.etrade.models.{Account, MarketSession, PortfolioView}
import com.odenzo.etrade.models.responses.ListAccountsRs
import com.odenzo.etrade.oauth.server.*
import com.odenzo.etrade.oauth.{ClientOAuth, OAuthClientMiddleware}
import org.http4s.client.Client

import java.time.LocalDate

/**
  * A Pure Backend App that uses etrade-client-lib to do login (and popup browser) Just a quick have to demonstrate logging in and pulling
  * some info. This has the default mode to load an access token from disk. Check if it works, if not, create a new one and override the one
  * on disk. Meh.... some autologin browser scraping thing better but I am sure its not straight forward.
  */
object BusinessMain {

  def business(resource: Resource[IO, (Client[IO], ETradeContext)]): IO[Unit] = {
    resource.use { (client, context) =>
      given ETradeContext = context
      given Client[IO]    = client

      import com.odenzo.etrade.api.commands.{Executable, given}

      for {
        accounts <- ListAccountsCmd().exec()
        _         = scribe.info(s"Accounts: ${pprint(accounts)}")
        account   = accounts.head
        accountId = account.accountIdKey
        _         = scribe.info(s"Your Account: ${pprint(account)}")
        balances <- AccountBalancesCmd(accountId, account.accountType.some, account.institutionType).exec()
        _         = scribe.info(s"Balances: ${pprint(balances)}")

        // BOTH START AND END or IGNORED
        txn1     <- ListTransactionsCmd(accountId, LocalDate.of(2021, 1, 1).some, LocalDate.of(2022, 5, 25).some, 45).exec()
        _         = scribe.info(s"Txns1: ${txn1.size} ${txn1}")
        txnByType = txn1.toList.groupBy(_.transactionType)
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
      } yield ()
    }

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
}
