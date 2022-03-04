package com.odenzo.etrade.oauth.client

import cats.effect.*
import org.http4s.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe.middleware.JsonDebugErrorHandler
import org.http4s.client.middleware.*
import org.http4s.client.*

/** Some generic BlazeClient builders, no real uue-case  or value added specific to OAuth. */
object OAuthClient {

  val logAction: Some[String => IO[Unit]] = Some((s: String) => IO(scribe.info(s"RQRS: $s")))

  val fullLogger: Client[IO] => Client[IO] =
    Logger(logHeaders = true, logBody = true, redactHeadersWhen = _ => false, logAction = logAction)

  /** Simple HTTP4S Client with no middleware */
  val clientR: Resource[IO, Client[IO]] = BlazeClientBuilder[IO].resource

  val simpleClient: Resource[IO, Client[IO]] = BlazeClientBuilder[IO].withDefaultSslContext.resource

  val debugClient: Resource[IO, Client[IO]] = {
    for {
      base  <- BlazeClientBuilder[IO].withRetries(1).withDefaultSslContext.resource
      logged = Logger(logHeaders = true, logBody = true, redactHeadersWhen = _ => false, logAction = logAction)(base)
    } yield logged
  }
}
