package com.odenzo.etrade.oauth

import cats.effect.{Deferred, IO, Resource}
import cats.implicits.*
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

  type WebCall[T] = Client[IO] ?=> T

  override val munitTimeout: FiniteDuration = Duration(10, "minutes")

  ScribeConfig.setupRoot(onlyWarnings = List("org.http4s.blaze"), initialLevel = scribe.Level.Info)

  test("Main") {
    val useLive: Boolean = false
    val url: Uri         = uri"https://api.etrade.com/"
    val sb: Uri          = uri"https://apisb.etrade.com/"
    val callbackUrl      = uri"http://localhost:5555/etrade/oauth_callback" // or 8888
    val redirectUrl      = uri"https://us.etrade.com/e/t/etws/authorize"
    // Consumer keys for SandBox and Live Environments

    val sbKey    = scala.sys.env("ETRADE_SANDBOX_KEY")
    val sbSecret = scala.sys.env("ETRADE_SANDBOX_SECRET")
    val key      = scala.sys.env.getOrElse("ETRADE_LIVE_KEY", "NO ETRADE_LIVE_KEY")
    val secret   = scala.sys.env.getOrElse("ETRADE_LIVE_SECRET", "NO ETRADE_LIVE_SECRET")

    val config =
      if useLive
      then OAuthConfig(oauthUrl = url, apiUrl = url, consumer = Consumer(key, secret), callbackUrl, redirectUrl)
      else OAuthConfig(oauthUrl = url, apiUrl = sb, consumer = Consumer(sbKey, sbSecret), callbackUrl, redirectUrl)

    val oauth                      = OAuth(config)
    scribe.info(s"Running Main Test with Config ${oprint(config)}")
    val done: IO[OAuthSessionData] = oauth.login()

  }
}
