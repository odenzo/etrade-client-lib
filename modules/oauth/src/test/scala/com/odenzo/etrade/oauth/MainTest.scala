package com.odenzo.etrade.oauth

import cats.effect.{Deferred, IO, Resource}
import com.github.blemale.scaffeine
import com.odenzo.base.OPrint.oprint
import com.odenzo.base.ScribeConfig
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

class MainTest extends munit.CatsEffectSuite {

  override val munitTimeout = Duration(10, "minutes")

  ScribeConfig.setupRoot(onlyWarnings = List("org.http4s.blaze"), initialLevel = scribe.Level.Info)

  test("Main") {
    val url: Uri = uri"https://api.etrade.com/"
    val callback = uri"http://localhost:5555/etrade/oauth_callback" // or 8888
    val key      = scala.sys.env("ETRADE_SANDBOX_KEY")
    val secret   = scala.sys.env("ETRADE_SANDBOX_SECRET")
    val config   = OAuthConfig(url, Consumer(key, secret), callback, uri"https://us.etrade.com/e/t/etws/authorize")

    val oauth = OAuth(config)
    scribe.info(s"Running Main Test with Config ${oprint(config)}")

    oauth.cacheR.use {
      cache =>
        val workerFN: IO[Unit] = waitForLoginAndProcess(id)

    }

    scribe.info(s"About to Start the OAUTH HTTP SERVER")
  }

  /** Noisy prog to fetch the request token */
  def requestToken(config: OAuthConfig)(using c: Client[IO]) =
    Authentication.requestToken(config.baseUrl, uri"oob", config.consumer)
      .flatTap(token => IO(scribe.info(s"Got  RequestToken: $token")))

  def initialLogin(oauth: OAuth, requestToken: Token): IO[OAuthSessionData] = Deferred[IO, OAuthSessionData].flatMap {
    (returnedData: Deferred[IO, OAuthSessionData]) =>
      oauth.serverR(returnedData).use {
        server =>
          for {
            _       <- IO(scribe.info(s"Running Server and Redirecting Browder"))
            rqtoken <- OAuthClient.simpleClient.use {
                         requestToken
                       }
            _       <- BrowserRedirect.redirectToETradeAuthorizationPage(config.redirectUrl, config.consumer, rqtoken)
            login   <- returnedData.get.timeout(5.minutes) // SemVar -- will "block" fiber until callback done.
          } yield login
      }
  }

}

object Data {}
