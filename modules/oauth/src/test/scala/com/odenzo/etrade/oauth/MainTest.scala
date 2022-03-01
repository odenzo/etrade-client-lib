package com.odenzo.etrade.oauth

import cats.effect.{IO, Resource}
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

    scribe.info(s"Running Main Test with Config ${oprint(config)}")

    val oauth = OAuth(config)
    val id    = UUID.randomUUID()
    oauth.cacheR.use {
      cache =>

        val appToken = OAuthClient.simpleClient.use {
          (c: Client[IO]) =>
            given Client[IO] = c

            Authentication.requestToken(config.baseUrl, uri"oob", config.consumer)
              .flatTap(token => IO(scribe.info(s"Got  RequestToken: $token")))
        }

        /** Defining this her to close on the cache. */
        def waitForLoginAndProcess(id: UUID) = IO.delay {
          val myCache = cache
          scribe.warn(s"User Logged In and I am using Cache $myCache")
        } *> IO.sleep(10.minutes) *> IO(scribe.info("Everything should close down now"))

        val workerFN: IO[Unit] = waitForLoginAndProcess(id)

        // Start the server running in the foreground forever
        scribe.info(s"About to Start the OAUTH HTTP SERVER")
        oauth.serverR(workerFN, cache).use {
          server =>
            val kickoff = for {
              _     <- IO(scribe.info(s"Running Server and Redirecting Browder"))
              token <- appToken
              _     <- BrowserRedirect.redirectToETradeAuthorizationPage(config.redirectUrl, config.consumer, token)
            } yield ()
            kickoff *> IO.never // This is the point we join really
        }
    }
  }
}

object Data {}
