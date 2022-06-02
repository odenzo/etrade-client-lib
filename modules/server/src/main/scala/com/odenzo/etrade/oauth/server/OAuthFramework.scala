package com.odenzo.etrade.oauth.server
import cats.syntax.all.*
import cats.effect.kernel.{Outcome, Resource}
import cats.effect.{Deferred, FiberIO, IO}
import com.odenzo.etrade.oauth.server.routes.OAuthCallbackServerApp
import com.odenzo.etrade.oauth.{ClientOAuth, OAuthClientMiddleware, OAuthConfig, OAuthSessionData}
import fs2.concurrent.SignallingRef
import org.http4s.{HttpApp, Uri}
import org.http4s.server.*
import org.http4s.client.*
import org.http4s.client.oauth1.Token

import scala.concurrent.duration.*

/**
  * This controls the E-TRADE API OAuth framework which can be used several ways:
  *   - Have the backend run a web server for etrade callback and open a browser for login. Closed server when login completed.
  *   - Have the backend run a web server and leave it running, allowing a front-end browser to control the UI/WebPage for logging in by
  *     query the web server for information. Calls can be made to close-down the webserver when needed.
  *   - Maybe we can allow the client of this lib to make their webserver and mix-in with their client/server needs. I don't really like
  *     this TBH so leave it as exercise to the reader :-)
  */
class OAuthFramework private (
    val config: OAuthConfig,
    val killSwitch: SignallingRef[IO, Boolean],
    val deferredSessionData: Deferred[IO, Either[Throwable, OAuthSessionData]]
) {
  private val host: String = config.callbackUrl.host.get.value
  private val port: Int    = config.callbackUrl.port.get

  def fetchRequestToken(using client: Client[IO]): IO[Token] = {
    ClientOAuth.getRequestToken(config)(using client: Client[IO])
  }

  /**
    * Creates a signing HTTPClient from scratch with the given tokens. Client has redirect and logging. This is a JVM only Ember client.
    * Convenience proxy
    */
  def constructSigningMiddlewareClient(sessionData: OAuthSessionData): Resource[IO, Client[IO]] = {
    WebFactory.baseClientR[IO].evalMap(base => OAuthClientMiddleware.standardWrapping(base, sessionData))
  }

  /**
    * This starts the WebServer for OAuth Callback and returns a Deffered. The main reason for seperation is to try and signal the front-end
    * *after* the WebServer has started so they don't redirect and do the login on e-trade and invoke the callback before webserver has
    * started. Its not "correct" in the sense we hae no way of knowing when the Ember server is "ready to accept connections", but good
    * enough for government work. TODO: need to monitor errors and signal those somehow also (server fatal errors like port in use etc)
    */
  def frontEndLauncher(appToken: Token)(using client: Client[IO]): IO[FiberIO[Unit]] = {
    val cbApp: HttpApp[IO]                        = OAuthCallbackServerApp.apps(config, appToken, deferredSessionData)
    scribe.info("About to Start the WebServer Fibre")
    // We should really complete the deferredOAuthSessionData with a negative value to signal error.
    // or have it an Either[Failed, OauthSessionData]
    // Alternative of just taking the Topic works for my app signalling but maybe not as flexible.
    val webserverFibre: IO[Either[Nothing, Unit]] = WebFactory.killableServer(host, port, cbApp, killSwitch) // Races on two seperate fibres
    webserverFibre
      .handleErrorWith { err =>
        scribe.error(s"Error in WebServer Fibre - I guess its shuts down?", err)
        deferredSessionData.complete(err.asLeft)
        killSwitch.getAndSet(true) // Kill it on any error just in case.
      }
      .guaranteeCase {
        case Outcome.Succeeded(fa) => IO(scribe.info(s"OauthServer Completed with $fa"))
        case Outcome.Errored(e)    => IO(scribe.info(s"OauthServer Comlpetion Error with ", e))
        case Outcome.Canceled()    => IO(scribe.info(s"OauthServer Completed via Cancellation"))
      }
      .void
      .start

  }

  /**
    * Spawns yet another background thread to check when the deferredSessionData is completed and then kills the WebServer. You can shut
    * down the server syncrhonously using the killSwitch directly or calling frontEndKiller. Returns the Fibre in case you want to join on
    * it or something. The Fibre is already started.
    */
  def frontEndKillWhenCompleteAsync(): IO[FiberIO[Either[Throwable, OAuthSessionData]]] = {
    val monitorThread =
      for {
        session <- deferredSessionData.get
        _       <- frontEndKiller()
      } yield session

    monitorThread.start
  }

  def frontEndKiller(): IO[Boolean] = killSwitch.getAndSet(true)

  /**
    * Pre-Made Helper that starts a webserver then launched a browser for humun login, accepts the callback then kills the WebServer This is
    * synchronous/blocking and OAuthSessionData will be returned. You should/can then make a MiddlwareClient out of that.
    */
  def backendBrowserLauncherKilling(browserLaunch: BrowserLaunchFn)(using client: Client[IO]): IO[Either[Throwable, OAuthSessionData]] = {

    for {
      appToken      <- ClientOAuth.getRequestToken(config)(using client)
      _              = scribe.info(s"Got Request Token: $appToken")
      redirectTo     = ClientOAuth.constructFullRedirectUrlForNoCallback(config, appToken)
      cbApp          = OAuthCallbackServerApp.apps(config, appToken, deferredSessionData)
      _             <- browserLaunch.open(redirectTo).delayBy(3.seconds)
      _              = scribe.info("About to Start the WebServer Fibre")
      cbServerFibre <- WebFactory.killableServer(host, port, cbApp, killSwitch).start // Races on two seperate fibres
      _             <- IO(scribe.info(s"Main Thread Should Be Waiting on Deferred ${deferredSessionData}"))
      session       <- deferredSessionData.get                                        // Wait for derred to be set (blocking)
      _             <- IO(scribe.info(s"Got the Defferred Session: ${session}"))
      _             <- killSwitch.set(true)                                           //  Use the kill switch to "cancel" the race with webserver
      _             <- IO(scribe.info(s"Waiting to Join the Fibre - Taking 20 second to kill!?"))
      // Is 20 seconds the default shutdown timeout? That is, shutdown on IO.race cancelling it not happening?
      _             <- cbServerFibre.join                                             // Join with completed shutdown webserver
      _             <- IO(scribe.info(s"Joined the WebServer fibre"))
    } yield session
  }

}

object OAuthFramework {

  /** Creates a framework for given config, injecting a kill-switch */
  def apply(config: OAuthConfig): IO[OAuthFramework] = {
    for {
      killSignalIO <- SignallingRef[IO, Boolean](false)
      deffered     <- Deferred[IO, Either[Throwable, OAuthSessionData]]
      framework     = new OAuthFramework(config, killSignalIO, deffered)
    } yield framework
  }

}
