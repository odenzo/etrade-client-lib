package com.odenzo.etrade.oauth.server

import cats.implicits.*
import cats.effect.unsafe.IORuntime
import cats.effect.unsafe.IORuntime.global
import cats.effect.{Deferred, IO, *}
import com.github.blemale.scaffeine
import com.github.blemale.scaffeine.Cache
import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.oauth.{OAuthSessionData, *}
import com.odenzo.etrade.oauth.config.OAuthConfig
import org.http4s.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.client.Client
import org.http4s.client.oauth1.Token
import org.http4s.dsl.io.*
import org.http4s.server.{Router, Server}

import java.util.UUID
import scala.concurrent.duration.*

class OAuthServer(underlying: Server)

/** This has one job, when the user logs into the web browser then browser invokes the callback and we extract login information. */
object OAuthServer {

  private object OptionalSyncQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Boolean]("sync")
  private object OAuthVerifierQPM              extends QueryParamDecoderMatcher[String]("oauth_verifier")
  private object OAuthTokenQPM                 extends QueryParamDecoderMatcher[String]("oauth_token")

  private def defineRoutes(config: OAuthConfig, returns: Deferred[IO, OAuthSessionData]): HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case GET -> Root / "version" =>
        scribe.info(s"version called")
        Ok("v1.0")

      /** This was defined when I asked for the keys, but can't find URL on etrade side to confirm it. */
      case GET -> Root / "etrade" / "oauth_callback" :? OAuthVerifierQPM(verifier) +& OAuthTokenQPM(auth_token) =>
        scribe.info(s"etrade/oauth_callback called")
        val session: IO[OAuthSessionData] = BlazeClientBuilder[IO].resource.use {
          client =>
            given Client[IO] = client
            for {
              _      <- IO(scribe.warn(s"Got the etrade login oauth callback: $verifier $auth_token"))
              id     <- IO(UUID.randomUUID())
              session = OAuthSessionData(id = id, accessToken = None, authToken = auth_token, verifier, config)
              // Careful about timing here, if we release the other thread it will close us donw before we can reply
              // Muck...
              _      <- IO(scribe.info("Have released the Deffered"))
              res    <- Ok("Thanks")
            } yield session
        }

        // Race condition, as soon as we release returns we can be shutdown.
        // This does the release and the Ok in parallel, obviously a hack but I am stumped
        // (IO.sleep(5.seconds) >>
        session.flatMap(s => returns.complete(s)) >> Ok("Completed Thanks")

    }

  /** A resource is created. */
  def createServer(
      host: String,
      port: Int,
      config: OAuthConfig,
      oauthData: Deferred[IO, OAuthSessionData]
  ): Resource[IO, Server] =
    scribe.info(s"Constructing Server Resource at $host:$port")

    val definedRoutes = defineRoutes(config, oauthData)
    // We bind the server params with the routes

    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withoutSsl
      .withHttpApp(Router("/" -> definedRoutes).orNotFound)
      .resource

}
