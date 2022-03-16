package com.odenzo.etrade.oauth

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.base.OPrint.oprint
import com.odenzo.etrade.oauth.client.OAuthClient
import com.odenzo.etrade.oauth.{Authentication, OAuthConfig, OAuthSessionData}
import fs2.concurrent.SignallingRef
import org.http4s.Uri.*
import org.http4s.client.Client
import org.http4s.client.oauth1.Token
import org.http4s.syntax.literals.uri
import org.http4s.{HttpRoutes, Uri}

import java.util.UUID
import scala.concurrent.duration.*

/**
  * This i an OAuth system that doesn't receive a callback with tokens, instead it needs theuer to cut and paste in the verifier 'keyword'.
  * Useful for both backends programs (I guess) but more so for WebBrowser applications. Peronally I make a system that supports a front-end
  * webapp but uses a backend to supply the accesss tokens (or sometimes just have the backend expose APIs to the front-end. Thats because I
  * mainly store stuff in DB etc which requires back-end. So, this is not really implemented.
  */
class OAuthNoCallback(val config: OAuthConfig) {

  def initiateLogin(): IO[Token] = requestTokenProg

  def complete(verifier: String, rqToken: Token): IO[OAuthSessionData] = {

    // import cats.effect.unsafe.implicits.global // IORunTime
    // BrowserRedirect.redirectToETradeAuthorizationPage(config.redirectUrl, config.consumer, rqToken)
    OAuthClient
      .oauthClient
      .use { client =>
        given Client[IO] = client

        for {
          access <- Authentication.getAccessToken(verifier, rqToken, config)
          _       = IO(scribe.info(s"Got ACCESS Token $access"))
          session = OAuthSessionData(accessToken = access, rqToken = rqToken, config)
        } yield (session)
      }
  }

  private[oauth] val requestTokenProg: IO[Token] = OAuthClient
    .oauthClient
    .use { client => Authentication.requestToken(config.oauthUrl, uri"oob", config.consumer)(using client: Client[IO]) }

}
