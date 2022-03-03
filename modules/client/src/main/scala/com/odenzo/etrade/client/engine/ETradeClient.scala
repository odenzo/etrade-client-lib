package com.odenzo.etrade.client.engine

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.syntax.all.*
import cats.effect.IO
import cats.effect.syntax.all.*
import com.odenzo.etrade.oauth.{Authentication, OAuthSessionData}
import com.odenzo.etrade.oauth.OAuthSessionData.Contextual
import io.circe.Decoder
import org.http4s.client.*
import org.http4s.*

/**
  * Initialized with a logged in Session, this is the gateway to use to call the APIs. It can be duplicated, client is MT safe.
  * @param session
  * @param client
  */
class ETradeClient(val session: OAuthSessionData, val c: Client[IO]) {

  given login: OAuthSessionData = session
  given client: Client[IO]      = c
  given baseUrl: Uri            = session.config.apiUrl
  type ERequest = Contextual[Request[IO]]

  def fetch[T: Decoder](rq: Contextual[Request[IO]]): IO[T] = IO.raiseError(Throwable("NIMP"))
  def debug[T: Decoder](rq: Contextual[Request[IO]]): IO[T] = IO.raiseError(Throwable("NIMP"))

  def fetch[T: Decoder](rq: Request[IO]): IO[T] = IO.raiseError(Throwable("NIMP"))
  def debug[T: Decoder](rq: Request[IO]): IO[T] = IO.raiseError(Throwable("NIMP"))

  def sign(rq: Request[IO]): IO[Request[IO]] =
    for {
      accessToken <- IO.fromOption(login.accessToken)(Throwable("Access Token has not been set"))
      signed      <- Authentication.sign(rq, accessToken, session.config.consumer)
    } yield signed

}

object ETradeClient:
  def validated(session: OAuthSessionData, c: Client[IO]): ValidatedNec[String, ETradeClient] =
    Validated.fromOption(session.accessToken, "Access Token is None")
      .map(_ => ETradeClient(session, c)) // Too Lazy to make an OAuthSession with non-optional access token but SHOULD
      .toValidatedNec
