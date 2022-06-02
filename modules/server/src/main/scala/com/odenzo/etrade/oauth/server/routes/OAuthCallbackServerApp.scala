package com.odenzo.etrade.oauth.server.routes

import cats.effect.IO.{IOCont, Uncancelable}
import cats.syntax.all.*
import cats.effect.{Deferred, IO}
import com.odenzo.etrade.oauth.{ClientOAuth, OAuthConfig, OAuthSessionData}
import org.http4s.*
import org.http4s.client.Client
import org.http4s.client.oauth1.Token
import org.http4s.dsl.*
import org.http4s.dsl.impl.*
import org.http4s.dsl.io.*
import org.http4s.headers.Location
import org.http4s.server.*
import org.http4s.syntax.all.*

import java.time.Instant
import cats.effect.{Deferred, IO}
import com.odenzo.etrade.oauth.ClientOAuth
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.client.Client
import org.http4s.client.oauth1.Token
import org.http4s.dsl.impl.{+&, ->, /, :?, QueryParamDecoderMatcher}
import org.http4s.dsl.io.{GET, Root}
import org.http4s.headers.Location
import org.http4s.server.Router

object OAuthCallbackServerApp {
  private object OAuthVerifierQPM extends QueryParamDecoderMatcher[String]("oauth_verifier")

  private object OAuthTokenQPM extends QueryParamDecoderMatcher[String]("oauth_token")

  def route(config: OAuthConfig, rqToken: Token, sessionD: Deferred[IO, Either[Throwable, OAuthSessionData]])(using
  Client[IO]): HttpRoutes[IO] = {
    val configuredETradeCallback: Path = config.callbackUrl.path

    HttpRoutes.of[IO] {
      case GET -> Root / "ping" => Ok("Alive")

      case GET -> Root / "config" =>
        import OAuthConfig.derived$AsObject
        import org.http4s.circe.CirceEntityCodec.*
        for {
          res <- Ok(config)
        } yield res

      case GET -> Root / "session" =>
        import org.http4s.circe.CirceEntityCodec.*
        for {
          attempt <- sessionD.tryGet
          res     <-
            attempt match
              case Some(Right(rs)) => Ok(rs)
              case Some(Left(err)) => NoContent()
              case None            => NotFound("Login Not Completed")
        } yield res

      case GET -> configuredETradeCallback :? OAuthVerifierQPM(verifier) +& OAuthTokenQPM(auth_token) =>
        scribe.info(s"SECURITY: OAuthCallback Called: Verifier: $verifier  auth_token: $auth_token")
        for {
          access <- ClientOAuth.getAccessToken(verifier, rqToken, config)
          _       = scribe.info(s"SECURITY: Fetched Access Token: $access")
          session = OAuthSessionData(accessToken = Some(access), rqToken = rqToken, config)
          res    <-
            config.postLoginRedirect match {
              case None           => Ok("You are logged in and can close this window.")
              case Some(redirect) => TemporaryRedirect(Location(redirect))
            }

          _ <- sessionD.complete(session.asRight) // Still a timing issue as this *may* race and have webserver cancelled before replying
        } yield res
    }
  }

  def apps(config: OAuthConfig, rqToken: Token, sessionD: Deferred[IO, Either[Throwable, OAuthSessionData]])(using
  Client[IO]): HttpApp[IO] = {
    val cb: HttpRoutes[IO] = route(config, rqToken, sessionD)
    Router[IO]("etrade" -> cb).orNotFound
  }
}
