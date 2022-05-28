package com.odenzo.etradeapp.purebackend

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.api.ETradeContext
import com.odenzo.etrade.api.commands.{*, given}
import com.odenzo.etrade.api.models.{ETradeApis, ETradeAuth, ETradeCallback, ETradeConfig, OAuthConfig, OAuthSessionData}
import com.odenzo.etrade.models.Account
import com.odenzo.etrade.models.responses.ListAccountsRs
import com.odenzo.etrade.oauth.{ClientOAuth, OAuthClientMiddleware}
import com.odenzo.etrade.oauth.server.*
import org.http4s.client.Client

/**
  * A Pure Backend App that uses etrade-client-lib to do login (and popup browser) Just a quick have to demonstrate logging in and pulling
  * some info. This has the default mode to load an access token from disk. Check if it works, if not, create a new one and override the one
  * on disk. Meh.... some autologin browser scraping thing better but I am sure its not straight forward.
  */
object PBEnd extends IOApp {
  val browserOpen = com.odenzo.etrade.oauth.server.BrowserLaunchFn.macMicrosoftEdge

  def run(args: List[String]) = {
    scribe.info("PURE BACKEND RUNNING")

    lazy val authEnv: ETradeAuth = ETradeAuth(scala.sys.env("ETRADE_LIVE_KEY"), scala.sys.env("ETRADE_LIVE_SECRET"))
    val etradeConfig             = ETradeConfig(false, ETradeCallback.default, localCallback = None, auth = authEnv, ETradeApis.defaultApis)
    val config                   = etradeConfig.asOAuthConfig

    WebFactory
      .baseClientR[IO]
      .evalMap(c => OAuthClientMiddleware.wrapLogger(true)(c))
      .use { baseClient =>
        given Client[IO] = baseClient
        for {
          framework   <- OAuthFramework(config)
          sessionData <- validateCachedTokens(config).flatMap {
                           case Some(value) => IO.pure(value)
                           case None        => doManualLogin(framework)
                         }
          context      = new ETradeContext(config.apiUrl)
          clientR      = framework.constructSigningMiddlewareClient(sessionData)
          resource     = clientR.map(c => (c, context))
          _            = scribe.info("Authenticated and Running Business Logic")
          _           <- BusinessMain.business(resource)
        } yield ExitCode.Success
      }
  }

  /** This is not part of the package reall,y just a dev hack. */
  def validateCachedTokens(config: OAuthConfig)(using client: Client[IO]): IO[Option[OAuthSessionData]] = {
    CachedAccessTokens
      .read()
      .redeem(err => Option.empty, tokens => tokens.some)
      .flatMap {
        case None                                           => Option.empty[OAuthSessionData].pure
        case Some(CachedAccessTokens(rqToken, accessToken)) =>
          for {
            _ <- ClientOAuth.refreshAccessToken(config, rqToken, accessToken)
          } yield Some(OAuthSessionData(accessToken.some, rqToken, config))
      }
  }

  def doManualLogin(framework: OAuthFramework)(using Client[IO]): IO[OAuthSessionData] = {
    scribe.info(s"Using Pure Backend Framework w/ Config: ${pprint(framework.config)}")
    framework
      .backendBrowserLauncherKilling(browserOpen)
      .flatMap {
        case Left(err)    => IO.raiseError(Throwable("Trouble in Authorization Process", err))
        case Right(value) => IO.pure(value)
      }
      .flatTap(session => CachedAccessTokens.write(CachedAccessTokens(session.rqToken, session.accessToken.get)))
  }

}
