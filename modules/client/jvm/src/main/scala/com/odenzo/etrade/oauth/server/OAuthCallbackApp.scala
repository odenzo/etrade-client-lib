package com.odenzo.etrade.oauth.server

import cats.effect.{Deferred, IO}
import com.odenzo.etrade.api.Authentication
import com.odenzo.etrade.api.models.{OAuthConfig, OAuthSessionData}
import org.http4s.*
import org.http4s.syntax.all.*
import org.http4s.client.Client
import org.http4s.client.oauth1.Token
import org.http4s.dsl.*
import org.http4s.dsl.impl.{+&, /, :?}
import org.http4s.dsl.io.*
import org.http4s.server.Router

import java.time.Instant

object OAuthCallbackApp:

  private object OptionalSyncQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Boolean]("sync")
  private object OAuthVerifierQPM              extends QueryParamDecoderMatcher[String]("oauth_verifier")
  private object OAuthTokenQPM                 extends QueryParamDecoderMatcher[String]("oauth_token")

  /** A resource is created. */
  def routes(config: OAuthConfig, rqToken: Token, sessionD: Deferred[IO, OAuthSessionData])(using client: Client[IO]): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "etrade" / "oauth_callback" :? OAuthVerifierQPM(verifier) +& OAuthTokenQPM(auth_token) =>
        for {
          access <- Authentication.getAccessToken(verifier, rqToken, config)
          session = OAuthSessionData(accessToken = access, rqToken = rqToken, config)
          _      <- sessionD.complete(session) // Still a timing issue
          res    <- Ok(s"OK - ${Instant.now()}  --- You can close this browser now if you want.")
        } yield res
    }

  def apps(config: OAuthConfig, rqToken: Token, session: Deferred[IO, OAuthSessionData])(using Client[IO]): HttpApp[IO] = {
    val r = routes(config = config, rqToken, session)
    Router("/" -> r).orNotFound
  }

end OAuthCallbackApp
