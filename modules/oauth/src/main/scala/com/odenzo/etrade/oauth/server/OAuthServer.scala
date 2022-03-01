package com.odenzo.etrade.oauth.server

import cats.implicits.*
import cats.effect.unsafe.IORuntime
import cats.effect.unsafe.IORuntime.global
import cats.effect.{Async, FiberIO, IO, Resource}
import com.github.blemale.scaffeine
import com.github.blemale.scaffeine.Cache
import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.oauth.*
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

/** This has one job, when the user logs into the web browser then browser invokes the callback and we extract login information. */
object OAuthServer {

  // Should learn about Ref and IO.ref which seems to make sense to use
  private var oauthWebServerFiber: Option[FiberIO[Nothing]] = None

  private object OptionalSyncQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Boolean]("sync")
  private object OAuthVerifierQPM              extends QueryParamDecoderMatcher[String]("oauth_verifier")
  private object OAuthTokenQPM                 extends QueryParamDecoderMatcher[String]("oauth_token")

  private def defineRoutes(config: OAuthConfig, cache: Cache[UUID, OAuthSessionData], workerFn: IO[Unit]): HttpRoutes[IO] = HttpRoutes
    .of[IO] {
      case GET -> Root / "version" =>
        scribe.info(s"version called")
        Ok("v1.0")

      case GET -> Root / "etrade" / "oauth_callback" :? OAuthVerifierQPM(verifier) +& OAuthTokenQPM(auth_token) =>
        BlazeClientBuilder[IO].resource.use {
          client =>
            given Client[IO] = client
            // use `client` here and return an `IO`.
            // the client will be acquired and shut down
            // automatically each time the `IO` is run.
            for {
              _      <- IO(scribe.warn(s"Got the etrade login oauth callback: $verifier $auth_token"))
              id     <- IO(UUID.randomUUID())
              session = OAuthSessionData(accessToken = None, verifier = verifier.some, id, reqToken = Token("huh", auth_token), config)

              // OAuthCallback
              // .fullPage(verifyer, auth_token))
              //  stopServer = IO.sleep(2.seconds).unsafeRunAsync(asyncShutdown)(IORuntime.global)
              res <- Ok("Thanks")
            } yield res
        }
    }

  /**
    * Note we have on server running at a time, so in theory we can leave it running and accept multiple or have sequential and start and
    * stop Creates without SSL (which is bad but easy...) The route is hard coded for e-trade instead of being passed in.
    */
  def createServer(
      host: String,
      port: Int,
      config: OAuthConfig,
      cache: scaffeine.Cache[UUID, OAuthSessionData],
      workerFn: IO[Unit]
  ): Resource[IO, Server] =
    scribe.info(s"Constructing Server Resource at $host:$port")

    val definedRoutes = defineRoutes(config, cache, workerFn)
    // We bind the server params with the routes

    BlazeServerBuilder[IO]
      .bindHttp(port, host)
      .withoutSsl
      .withHttpApp(Router("/" -> definedRoutes).orNotFound)
      .resource

  def asyncShutdown(r: Either[Throwable, Unit]) = {
    scribe.info(s"Handled Callback and Slept 2 second, $r NOW shutting down the server by cancelling fiber")
    r match {
      case Right(err) => this.stopServer()
      case Left(v)    => this.stopServer()
    }
  }

  def stopServer(): Unit = {
    scribe.info(s"Stopping the Server $oauthWebServerFiber")
    IO.fromOption(oauthWebServerFiber)(new Throwable("No WebServer is Running"))
      .flatMap {
        fiber =>
          scribe.info(s"Issuing the fiber cancel")
          fiber.cancel
      }
      .unsafeRunSync()(IORuntime.global)
  }

}
