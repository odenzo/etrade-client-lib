package com.odenzo.etradeapp.purebackend

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.api.{ETradeApis, ETradeAuth, ETradeCallback, ETradeConfig, ETradeContext}
import com.odenzo.etrade.api.commands.{*, given}
import com.odenzo.etrade.models.Account
import com.odenzo.etrade.models.responses.ListAccountsRs
import com.odenzo.etrade.oauth.{ClientOAuth, OAuthClientMiddleware, OAuthConfig, OAuthSessionData}
import com.odenzo.etrade.oauth.server.*
import org.http4s.client.Client

/**
  * A Pure Backend App that uses etrade-client-lib to do login (and popup browser). This has a very insecure hack of caching the tokens on
  * disk so you can run multiple times w/o logging in again. More of a demo on the command based way to run things.
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
          sessionData <- sessionFromValidatedCachedTokens(config).redeemWith(_ => doManualLogin(framework), bind => bind.pure)
          context      = new ETradeContext(config.apiUrl)
          clientR      = framework.constructSigningMiddlewareClient(sessionData)
          resource     = clientR.map(c => (c, context))
          _            = scribe.info("Authenticated and Running Business Logic")
          _           <- BusinessMain.business(resource)
        } yield ExitCode.Success
      }
  }

  /** This is not part of the package reall,y just a dev hack. */
  def sessionFromValidatedCachedTokens(config: OAuthConfig)(using client: Client[IO]): IO[OAuthSessionData] = {
    val prog =
      for {
        tokens <- CachedAccessTokens.read()
        session = OAuthSessionData(tokens.access.some, tokens.request, config)
        _      <- ClientOAuth.refreshAccessToken(session)
      } yield session

    prog.onError(_ => CachedAccessTokens.clearCache())

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
