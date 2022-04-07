package com.odenzo.etrade.oauth.server

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.api.Authentication
import com.odenzo.etrade.base.OPrint.oprint
import com.odenzo.etrade.api.models.*
import com.odenzo.etrade.oauth.client.OAuthClientMiddleware
import com.odenzo.etrade.oauth.server.BrowserRedirectFn
import com.odenzo.etrade.oauth.server.OAuthServer
import com.odenzo.etrade.oauth.Authentication
import com.odenzo.etrade.oauth.client.middleware.OAuthClientMiddleware
import fs2.concurrent.SignallingRef
import org.http4s.Uri.*
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.client.Client
import org.http4s.client.oauth1.Token
import org.http4s.server.{Router, Server}
import org.http4s.syntax.literals.uri
import org.http4s.{HttpRoutes, Uri}

import java.util.UUID
import scala.concurrent.duration.*

/**
  * Main class to instanciate the system for a login, or multiple logins to a partocular host with app consumer keys. This should turn into
  * a facade that supports both initial sign-in/oauth and the refresh information, and the oauth cache ?
  */
class OAuthCallback(val config: OAuthConfig) {

  /** Standard login were this will call a functionk to popup a new browser to login with */
  def login(fn: BrowserRedirectFn): IO[OAuthSessionData] = {
    for {
      appToken <- requestTokenProg
      redirect  = BrowserRedirect.buildRedirectUrl(config, appToken)
      session  <- this.callbackLogin(appToken, redirect, fn)
    } yield session
  }

  /**
    * Returns the URL to redirect to while running the server async in the background. Once the server completes get the OAuthSession It
    * won't open up a Browser session. You will
    */
  def asyncLogin(): IO[PartialLogin] = {
    for {
      appToken <- requestTokenProg
      redirect  = BrowserRedirect.buildRedirectUrl(config, appToken)
      session   = this.callbackLogin(appToken, redirect, BrowserRedirect.noop)
    } yield PartialLogin(redirect, session)
  }

  // val killSwitch                    = fs2.concurrent.Signal[IO, Boolean]
  private[oauth] val killSwitch: IO[SignallingRef[IO, Boolean]] = SignallingRef[IO, Boolean](false)

  private[oauth] val requestTokenProg: IO[Token] = OAuthClientMiddleware
    .oauthClient
    .use { client => Authentication.requestToken(config.oauthUrl, uri"oob", config.consumer)(using client: Client[IO]) }

  /**
    * This starts a webserver with the callback route and BLOCKS (logically) until times out or gets login callback from etrade.
    * @param appToken
    *   Request Token to verify the application
    * @param redirectUrl
    *   URL to open a browser and redirect to.
    * @param redirectFn
    *   Function to use to open browser, may be a no-op function is you take care of it.
    * @return
    */
  def callbackLogin(appToken: Token, redirectUrl: Uri, redirectFn: BrowserRedirectFn): IO[OAuthSessionData] = {
    import cats.effect.unsafe.implicits.global // IORunTime
    for {
      returnData      <- Deferred[IO, OAuthSessionData]
      res             <- OAuthServer.serverScopedR(appToken, config, returnData)
      (stream, killer) = res
      _               <- redirectFn(redirectUrl)
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

}
