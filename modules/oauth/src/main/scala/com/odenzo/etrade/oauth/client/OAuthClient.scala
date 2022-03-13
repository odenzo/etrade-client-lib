package com.odenzo.etrade.oauth.client

import cats.effect.{IO, Resource, *}
import com.odenzo.etrade.oauth.OAuthSessionData
import org.http4s.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe.middleware.JsonDebugErrorHandler
import org.http4s.client.middleware.*
import org.http4s.client.*

/** Some generic BlazeClient builders, no real uue-case  or value added specific to OAuth. */
object OAuthClient {

  val logAction: Some[String => IO[Unit]] = Some((s: String) => IO(scribe.info(s"RQRS: $s")))

  val fullLogger: Client[IO] => Client[IO] = Logger(
    logHeaders = true,
    logBody = true,
    redactHeadersWhen = _ => false,
    logAction = logAction
  )

  /** Simple HTTP4S Client with no middleware */
  val clientR: Resource[IO, Client[IO]] = BlazeClientBuilder[IO].resource

  val simpleClient: Resource[IO, Client[IO]] = BlazeClientBuilder[IO].withDefaultSslContext.resource

  /** OAuthClient that has redirect and logging, for use with OAuth module */
  val oauthClient: Resource[IO, Client[IO]] = {
    for {
      base    <- simpleClient
      logged   = Logger(logHeaders = true, logBody = true, redactHeadersWhen = _ => false, logAction = logAction)(base)
      redirect = FollowRedirect(3)(logged)
    } yield redirect
  }

  val debugClient: Resource[IO, Client[IO]] = {
    for {
      base  <- BlazeClientBuilder[IO].withRetries(1).withDefaultSslContext.resource
      logged = Logger(logHeaders = true, logBody = true, redactHeadersWhen = _ => false, logAction = logAction)(base)

    } yield logged
  }

  /**
    * Signing Client that follows redirects with no logging. Add your own or build your own from scratch, see OAuthStatusSigner middleware
    */
  def signingClient(oauthSesssion: OAuthSessionData): Resource[IO, Client[IO]] = {
    for {
      base    <- BlazeClientBuilder[IO].withDefaultSslContext.resource
      signing  = OAuthStaticSigner(oauthSesssion)(base)
      redirect = FollowRedirect(3)(signing)
    } yield redirect
  }

  def signingDebugClient(oauthSesssion: OAuthSessionData): Resource[IO, Client[IO]] = {
    for {
      base    <- BlazeClientBuilder[IO].withDefaultSslContext.resource
      signing  = OAuthStaticSigner(oauthSesssion)(base)
      redirect = FollowRedirect(3)(signing)
      logging  = Logger(logHeaders = true, logBody = true, redactHeadersWhen = _ => false, logAction = logAction)(redirect)
    } yield logging
  }

}
