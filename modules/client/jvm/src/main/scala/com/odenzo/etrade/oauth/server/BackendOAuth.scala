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

import com.odenzo.etrade.oauth.server.OAuthWebServer
import com.odenzo.etrade.oauth.OAuthLogic
import com.odenzo.etrade.oauth.client.middleware.OAuthClientMiddleware
import com.odenzo.etrade.oauth.server.BrowserLaunch.BrowserRedirectFn
import fs2.concurrent.SignallingRef
import org.http4s.Uri.*
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
class BackendOAuth(val config: OAuthConfig) {

  /**
    * Standard login were this will call a functionk to popup a new browser to login with. This works find for the JVM Style. And if url =>
    * IO is enough to model a callback in JS land maybe this will work for bth.
    */
  def login(fn: BrowserRedirectFn)(using client: Client[IO]): IO[OAuthSessionData] = {

    val deferredIO                                   = Deferred[IO, OAuthSessionData]
    val killSignalIO: IO[SignallingRef[IO, Boolean]] = SignallingRef[IO, Boolean](false)

    for {
      appToken      <- OAuthLogic.initiateLogin(config)(using client)
      redirectTo     = OAuthLogic.constructFullRedirectUrlForNoCallback(config, appToken)
      deferred      <- deferredIO
      loadedGun     <- killSignalIO
      cbApp          = OAuthCallbackApp.apps(config, appToken, deferred)
      cbServerFibre <- OAuthWebServer.killableServer("localhost", 5555, cbApp, loadedGun).start // Races on two seperate fibres
      session       <- deferred.get                                                             // Wait for derred to be set (blocking)
      _             <- loadedGun.set(true)                                                      //  Use the kill switch to "cancel" the race with webserver
      // _ <- cbServerFibre.cancel  // Could have juwt use this instead of the kill switch.
      _             <- cbServerFibre.join                                                       // Join with completed shutdown webserver
    } yield session
  }

}
