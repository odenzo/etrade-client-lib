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
import org.http4s.syntax.all.*
import org.http4s.Status.*
import org.http4s.*

/**
  * Initialized with a logged in Session, this is the gateway to use to call the APIs. It can be duplicated, client is MT safe.
  * @param session
  * @param client
  *   TMFA (too much...) Thi is no a pre-configued Client with Signing middleware
  */
class ETradeClient(val config: ETradeContext, val c: Client[IO]) {

  given ETradeContext      = config
  given client: Client[IO] = c

  def fetchCF[T: Decoder](rq: ETradeCall): IO[T] = rq.flatMap(r => run(r))
  def debugCF[T: Decoder](rq: ETradeCall): IO[T] = IO.raiseError(Throwable("NIMP"))

  // These are called if the context functions are resolved. Note that overloading  A ?=> B and B doens't work
  // Maybe something to do with optional context parameters, or becaue we have given login in scope Dunno.
  // Better to be explicit anything

  def fetch[T: Decoder](rq: Request[IO]): IO[T] = run(rq)
  def debug[T: Decoder](rq: Request[IO]): IO[T] = IO.raiseError(Throwable("NIMP"))

  private def run[T: Decoder](rq: Request[IO]) =
    // import      org.http4s.circe.CirceSensitiveDataEntityDecoder.*
    import org.http4s.circe.CirceEntityDecoder.*
    client.expectOr[T](rq)(rs => handleHttpErrors[T](rq, rs))

  def handleHttpErrors[T](rq: Request[IO], rs: Response[IO]): IO[Throwable] = {
    rs.bodyText.compile.string.map(body => Throwable(s"Crude HTTP Error: ${rs.status} ${body}"))
  }
}
