package com.odenzo.etrade.oauth

import cats.effect.kernel.Outcome
import com.odenzo.base.OLogging
import com.odenzo.etrade.client.authentication.Authentication
import cats.effect.{FiberIO, IO, Ref}
import org.http4s.server.Router
import org.http4s.client.Client
import cats.effect.unsafe.IORuntime
import com.odenzo.etrade.client.sdk.BusinessMain
import org.http4s._
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.dsl.io._

import scala.concurrent.duration.DurationInt

object OAuthServer extends OLogging {

  // Should learn about Ref and IO.ref which seems to make sense to use
  private var oauthWebServerFiber: Option[FiberIO[Nothing]] = None

  private object OptionalSyncQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Boolean]("sync")
  private object OAuthVerifierQPM              extends QueryParamDecoderMatcher[String]("oauth_verifier")
  private object OAuthTokenQPM                 extends QueryParamDecoderMatcher[String]("oauth_token")

  private def routes(implicit client: Client[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "version" =>
      scribe.info(s"version called")
      Ok("v1.0")

    case GET -> Root / "etrade" / "oauth_callback" :? OAuthVerifierQPM(verifier) +& OAuthTokenQPM(auth_token) => for {
        _         <- IO(scribe.warn("Got the etrade login oauth callback"))
        session   <- Authentication.handleCallback("default", verifier, auth_token)
        done      <- Ok(s"$verifier and $auth_token being use to do some backend work") // OAuthCallback.fullPage(verifyer, auth_token))
        stopServer = IO.sleep(2.seconds).unsafeRunAsync(asyncShutdown)(IORuntime.global)
      } yield done

  }

  def asyncShutdown(r: Either[Throwable, Unit]) = {
    scribe.info(s"Handled Callback and Slept 2 second, $r NOW shutting down the server by cancelling fiber")
    r match {
      case Right(err) => this.stopServer()
      case Left(v)    => this.stopServer()
    }
  }

  /** This starts the server running asynchronously. */
  def startServer(implicit client: Client[IO], rt: IORuntime): IO[FiberIO[Nothing]] = {
    IO {
      if (oauthWebServerFiber.isDefined) throw new Throwable("OAuthWebserver Running Already....")
      else {
        val httpApp = Router("/" -> routes).orNotFound

        val fiber = BlazeServerBuilder[IO]
          .bindHttp(5555, "localhost")
          .withoutSsl
          .withHttpApp(httpApp)
          .resource
          .use(_ => IO.never)
          .start
          .unsafeRunSync()
        this.oauthWebServerFiber = Some(fiber)
        fiber
      }
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
