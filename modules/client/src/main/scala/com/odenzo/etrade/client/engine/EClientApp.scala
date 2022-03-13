package com.odenzo.etrade.client.engine

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.effect.IO
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.oauth.OAuthSessionData.Contextual
import com.odenzo.etrade.oauth.{Authentication, OAuthSessionData}
import io.circe.Decoder
import org.http4s.*
import org.http4s.Status.*
import org.http4s.client.*
import org.http4s.syntax.all.*

/**
  * Initialized with a logged in Session, this is the gateway to use to call the APIs. It can be duplicated, client is MT safe.
  *
  * @param session
  * @param client
  */
class EClientApp(val session: OAuthSessionData, val c: Client[IO]) {

  given login: OAuthSessionData = session
  given client: Client[IO]      = c
  given baseUrl: Uri            = session.config.apiUrl
  type ERequest = Contextual[Request[IO]]

  def fetchCF[T: Decoder](rq: Contextual[Request[IO]]): IO[T] = run(rq)
  def debugCF[T: Decoder](rq: Contextual[Request[IO]]): IO[T] = IO.raiseError(Throwable("NIMP"))

  // These are called if the context functions are resolved. Note that overloading  A ?=> B and B doens't work
  // Maybe something to do with optional context parameters, or becaue we have given login in scope Dunno.
  // Better to be explicit anything

  def fetch[T: Decoder](rq: Request[IO]): IO[T] = run(rq)
  def debug[T: Decoder](rq: Request[IO]): IO[T] = IO.raiseError(Throwable("NIMP"))

  val signingApp: ReaderT[IO, Request[IO], Response[IO]] = client.toHttpApp.compose(rq => sign(rq))
  // val command = signingApp.mapF()

  def sign(rq: Request[IO]): IO[Request[IO]] = Authentication.sign(rq, session.accessToken, session.config.consumer)

  private def run[T: Decoder](rq: Request[IO]) =
    // import      org.http4s.circe.CirceSensitiveDataEntityDecoder.*
    import org.http4s.circe.CirceEntityDecoder.*
    sign(rq).flatMap(rq => client.expectOr[T](rq)(rs => handleHttpErrors[T](rq, rs)))

  def handleHttpErrors[T](rq: Request[IO], rs: Response[IO]): IO[Throwable] = {
    IO(Throwable(s"Crude HTTP Error: ${rs.status}"))
  }

}
