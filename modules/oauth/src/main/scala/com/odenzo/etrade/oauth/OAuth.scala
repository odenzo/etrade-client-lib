package com.odenzo.etrade.oauth

import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.syntax.all.*
import com.github.blemale.scaffeine
import com.odenzo.etrade.base.OPrint.oprint
import com.odenzo.etrade.oauth.client.{BrowserRedirect, OAuthClient}
import com.odenzo.etrade.oauth.config.OAuthConfig
import com.odenzo.etrade.oauth.server.OAuthServer
import fs2.concurrent.SignallingRef
import org.http4s.{HttpRoutes, Uri}
import org.http4s.Uri.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.client.Client
import org.http4s.client.oauth1.Token
import org.http4s.server.{Router, Server}
import org.http4s.syntax.literals.uri

import scala.concurrent.duration.*
import java.util.UUID

/**
  * Main class to instanciate the system for a login, or multiple logins to a partocular host with app consumer keys. This should turn into
  * a facade that supports both initial sign-in/oauth and the refresh information, and the oauth cache ?
  */
class OAuth(val config: OAuthConfig) {
  def login(): IO[OAuthSessionData] = this.fullLogin

  // val killSwitch                    = fs2.concurrent.Signal[IO, Boolean]
  private[oauth] val killSwitch: IO[SignallingRef[IO, Boolean]] = SignallingRef[IO, Boolean](false)

  private[oauth] val browserLoginAndAccessToken: Token => IO[OAuthSessionData] =
    (rqToken: Token) => {
      import cats.effect.unsafe.implicits.global // IORunTime
      for {
        returnData      <- Deferred[IO, OAuthSessionData]
        res             <- OAuthServer.serverScopedR(rqToken, config, returnData)
        (stream, killer) = res
        _               <- BrowserRedirect.redirectToETradeAuthorizationPage(config.redirectUrl, config.consumer, rqToken)
        _                = stream
                             .compile
                             .last
                             .unsafeRunAsyncOutcome {
                               case Canceled()    => scribe.error("WebServer canceled !")
                               case Errored(e)    => scribe.error("WebServer Error", e)
                               case Succeeded(fa) => scribe.warn(s"WebServer Completed As Expected $fa")
                             }
        login           <- returnData.get.timeout(1.minute) // SemVar -- will "block" fiber until callback done.
        _               <- IO.sleep(2.seconds) *> IO(scribe.info("Killing...")) *> killer.update(_ => true)
        _               <- IO(scribe.info(s"OK - We are all logged in... client and server and cache running: ${oprint(login)}"))

      } yield login
    }

  private[oauth] val requestTokenProg: IO[Token] = OAuthClient
    .oauthClient
    .use { client => Authentication.requestToken(config.oauthUrl, uri"oob", config.consumer)(using client: Client[IO]) }

  // Ok - we are ready to run.
  private[oauth] val fullLogin: IO[OAuthSessionData] = requestTokenProg.flatMap(token => browserLoginAndAccessToken(token))

  // Example Client Main See TestMain
}
