package com.odenzo.etrade.oauth.server

import cats.implicits.*
import cats.effect.unsafe.*
import cats.effect.unsafe.IORuntime.global
import cats.effect.*
import cats.effect.implicits.*
import com.github.blemale.scaffeine
import com.github.blemale.scaffeine.Cache
import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.oauth.client.OAuthClient
import com.odenzo.etrade.oauth.{OAuthSessionData, *}
import com.odenzo.etrade.oauth.config.OAuthConfig
import org.http4s.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.client.Client
import org.http4s.client.oauth1.Token
import org.http4s.dsl.io.*
import org.http4s.server.{Router, Server}

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration.*

class OAuthServer(underlying: Server)

/** This has one job, when the user logs into the web browser then browser invokes the callback and we extract login information. */
object OAuthServer {
  import cats.effect.unsafe.implicits.global
  private object OptionalSyncQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Boolean]("sync")
  private object OAuthVerifierQPM              extends QueryParamDecoderMatcher[String]("oauth_verifier")
  private object OAuthTokenQPM                 extends QueryParamDecoderMatcher[String]("oauth_token")

  /** A resource is created. */
  def routes(config: OAuthConfig, rqToken: Token, sessionD: Deferred[IO, OAuthSessionData]): HttpRoutes[IO] =
    HttpRoutes.of[IO] {
      case GET -> Root / "etrade" / "oauth_callback" :? OAuthVerifierQPM(verifier) +& OAuthTokenQPM(auth_token) =>
        OAuthClient.simpleClient.use {
          scopedClient =>
            given Client[IO] = scopedClient
            for {
              _      <- IO(scribe.warn(s"Got the etrade login oauth callback: $verifier $auth_token"))
              id     <- IO(UUID.randomUUID())
              access <- Authentication.getAccessToken(verifier, rqToken, auth_token, config)
              _       = IO(scribe.info(s"Got ACCESS Token $access"))
              session = OAuthSessionData(id = id, accessToken = None, authToken = auth_token, verifier, config)
              _      <- sessionD.complete(session) // Still a timing issue
              res    <- Ok(s"OK - ${Instant.now()}")
            } yield res
        }
    }

}
