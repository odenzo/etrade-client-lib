package com.odenzo.etrade

import cats.*
import cats.data.ValidatedNec
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.github.blemale.scaffeine
import com.odenzo.base.OPrint.oprint
import com.odenzo.base.ScribeConfig
import com.odenzo.etrade.client.engine.{ETradeClient, ETradeContext}
import com.odenzo.etrade.models.Account
import com.odenzo.etrade.models.responses.ListAccountsRs
import com.odenzo.etrade.oauth.*
import com.odenzo.etrade.oauth.client.*
import com.odenzo.etrade.oauth.config.OAuthConfig
import org.http4s.Uri
import org.http4s.Uri.{*, given}
import org.http4s.client.Client
import org.http4s.client.oauth1.{Consumer, Token}
import org.http4s.server.Server
import org.http4s.syntax.all.{*, given}

import java.util.UUID
import scala.concurrent.duration.*

/** Simulates a main app using the E-Trade Client Lib */
object Main extends IOApp:

  ScribeConfig.setupRoot(onlyWarnings = List("org.http4s.blaze"), initialLevel = scribe.Level.Info)

  /** Creates an OAuthConfig necessary to initializing and using the E-Trade OAuth login and request signing */
  def createConfig(args: List[String]): IO[OAuthConfig] = IO {
    val useLive: Boolean = true
    val url: Uri         = uri"https://api.etrade.com/"
    val sb: Uri          = uri"https://apisb.etrade.com/"
    val callbackUrl      = uri"http://localhost:5555/etrade/oauth_callback" // or 8888
    val redirectUrl      = uri"https://us.etrade.com/e/t/etws/authorize"
    // Consumer keys for SandBox and Live Environments

    // Crash the program if the environment doesn't have both sets of keys :-( (error in IO context)
    val sbKey    = scala.sys.env("ETRADE_SANDBOX_KEY")
    val sbSecret = scala.sys.env("ETRADE_SANDBOX_SECRET")
    val key      = scala.sys.env("ETRADE_LIVE_KEY")
    val secret   = scala.sys.env("ETRADE_LIVE_SECRET")

    if useLive
    then OAuthConfig(oauthUrl = url, apiUrl = url, consumer = Consumer(key, secret), callbackUrl, redirectUrl)
    else OAuthConfig(oauthUrl = url, apiUrl = sb, consumer = Consumer(sbKey, sbSecret), callbackUrl, redirectUrl)
  }
  // As a lib we want to try and make things flexible for apps using their own Client[IO]
  // but we also need to deal with the OAuthSessionData mutating when/if we need to refresh the access token
  // A nasty hack is to either keep state in the Client via middleware or to use a mutable Cache.
  // We can avoid both in the e-trade use case by having a background "tickle" the access token so it doesn't expire.
  // At midnight EST we loose the session, and cannot recover an access token, must manually login the oauth
  // so there is no real choice but to die.

  def run(args: List[String]): IO[ExitCode] =
    for {
      _       <- IO(scribe.info("Running..."))
      config  <- createConfig(args)
      rqConfig = ETradeContext(config.apiUrl) // ETradeContext has context functions to aid in helping Request Creation
      oauth    = OAuth(config)
      login   <- oauth.login()                // Undecided what to do with this currently this forces manual login by opening web browser
      res     <- OAuthClient.signingClient(login).use(cio => BusinessMain.run(ETradeClient(rqConfig, cio)))
    } yield res
end Main
