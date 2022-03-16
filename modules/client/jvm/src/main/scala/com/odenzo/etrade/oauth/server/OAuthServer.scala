package com.odenzo.etrade.oauth.server

import cats.effect.*
import cats.effect.implicits.*
import cats.effect.unsafe.*
import cats.effect.unsafe.IORuntime.global
import cats.implicits.*
import com.odenzo.etrade.base.OPrint.oprint
import com.odenzo.etrade.client.models.{OAuthConfig, OAuthSessionData}
import com.odenzo.etrade.oauth.client.OAuthClient
import com.odenzo.etrade.oauth.*
import fs2.concurrent.SignallingRef
import org.http4s.*
import org.http4s.Uri.RegName
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
  def routes(config: OAuthConfig, rqToken: Token, sessionD: Deferred[IO, OAuthSessionData]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "etrade" / "oauth_callback" :? OAuthVerifierQPM(verifier) +& OAuthTokenQPM(auth_token) =>
      OAuthClient
        .simpleClient
        .use {
          scopedClient =>
            given Client[IO] = scopedClient
            for {
              access <- Authentication.getAccessToken(verifier, rqToken, config)
              session = OAuthSessionData(accessToken = access, rqToken = rqToken, config)
              _      <- sessionD.complete(session) // Still a timing issue
              res    <- Ok(s"OK - ${Instant.now()}  --- You can close this browser now if you want.")
            } yield res
        }
  }

  /**
    * @param rqToken
    *   Rrquest token needed to get the access token.
    * @param config
    *   Details on callback URL
    * @param answerD
    *   The answer we get from the callback as a deffered that is completed on succesful completion.
    * @return
    *   A Stream that when run is a tuple of the running web server and a killswitch that is supposed to cleanly shutdown the webserver. So,
    *   the webserver client (the routes) gets the callback, if its OK then it comletes the answer. Whwn the other thread waiting on the
    *   defferred answer gets it, it toggles the kill switch. Because not kosher to kill the webserver while its processing a request. This
    *   is still not theoretically perfect, because we could still kill the server too early and it doesn't response to callback but in this
    *   case it doesn't really matter.
    */
  def serverScopedR(
      rqToken: Token,
      config: OAuthConfig,
      answerD: Deferred[IO, OAuthSessionData]
  ): IO[(fs2.Stream[IO, ExitCode], SignallingRef[IO, Boolean])] =
    val host: String = config.callbackUrl.host.getOrElse(RegName("localhhost")).value
    val port: Int    = config.callbackUrl.port.getOrElse(5555)

    val routes: HttpRoutes[IO]                       = OAuthServer.routes(config = config, rqToken, answerD)
    val killSwitchIO: IO[SignallingRef[IO, Boolean]] = SignallingRef[IO, Boolean](false)
    val exitCode                                     = Ref[IO].of(ExitCode.Success)
    for {
      killer   <- SignallingRef[IO, Boolean](false)
      exitcode <- Ref[IO].of(ExitCode.Success)
      server    = BlazeServerBuilder[IO]
                    .bindHttp(port, host)
                    .withoutSsl
                    .withHttpApp(Router("/" -> routes).orNotFound)
                    .serveWhile(killer, exitWith = exitcode)
    } yield (server, killer)
  end serverScopedR

}
