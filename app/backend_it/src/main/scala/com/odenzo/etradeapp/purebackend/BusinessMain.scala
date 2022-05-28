package com.odenzo.etradeapp.purebackend

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.api.ETradeContext
import com.odenzo.etrade.api.commands.{*, given}
import com.odenzo.etrade.api.models.*
import com.odenzo.etrade.models.Account
import com.odenzo.etrade.models.responses.ListAccountsRs
import com.odenzo.etrade.oauth.server.*
import com.odenzo.etrade.oauth.{ClientOAuth, OAuthClientMiddleware}
import org.http4s.client.Client

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

      val M = summon[Executable[ListAccountsCmd, List[Account]]]
      for {
        accounts <- ListAccountsCmd().exec()
        _         = scribe.info(s"Accounts: ${pprint(accounts)}")
        account   = accounts.head
        _         = scribe.info(s"Your Account: ${pprint(account)}")
      } yield ()
    }

  }

}
