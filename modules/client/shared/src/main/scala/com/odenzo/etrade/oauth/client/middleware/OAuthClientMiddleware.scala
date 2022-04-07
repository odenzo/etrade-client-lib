package com.odenzo.etrade.oauth.client.middleware

import cats.effect.implicits.*
import cats.effect.*
import cats.syntax.all.*
import com.odenzo.etrade.api.Authentication
import com.odenzo.etrade.api.models.*
import org.http4s.*

import org.http4s.circe.middleware.JsonDebugErrorHandler
import org.http4s.client.middleware.*
import org.http4s.client.*

/**
  * Some generic BlazeClient builders, no real uue-case or value added specific to OAuth. TODO: Clean this up, and makr oauth only ones
  * private to oauth package. Woud be nice to make a decent logger based on content type of the entity too. TODO: We now want to wrap both
  * resource clients and plain old clients for http4s-dom client.
  */
object OAuthClientMiddleware {

  def wrapOAuthStaticSigner(session: OAuthSessionData)(client: Client[IO]) = OAuthStaticSigner.apply(session)(client)

  def wrapLogger(logBody: Boolean)(client: Client[IO]): Client[IO] =
    val logAction: Some[String => IO[Unit]] = Some((s: String) => IO(scribe.info(s"RQRS: $s")))
    Logger(
      logHeaders = true,
      logBody = true,
      redactHeadersWhen = _ => false,
      logAction = logAction
    )(client)

  /** Simple HTTP4S Client with no middleware */
  // Check what the deal with this is, perhaps not ScalaJS able
  // def wrapGZip(client: Client[IO]): Client[IO] = org.http4s.client.middleware.GZip.apply[IO](32 * 1024)(client)

  /** OAuthClient that has redirect and logging, for use with OAuth module */
  def wrapRedirect(max: Int = 3)(client: Client[IO]): Client[IO] = FollowRedirect[IO](max)(client)

  // Not sure we can use cookie jar easily in dom client... not this is in IO coming out.
  def wrapCookie(client: Client[IO]): IO[Client[IO]] = CookieJar.impl(client)

}
