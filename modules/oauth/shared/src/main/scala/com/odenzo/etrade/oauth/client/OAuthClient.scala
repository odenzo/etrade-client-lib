package com.odenzo.etrade.oauth.client

import cats.effect.{IO, Resource, *}
import cats.syntax.all.*
import com.odenzo.etrade.oauth.OAuthSessionData
import org.http4s.*
import org.http4s.ember.client.*
import org.http4s.circe.middleware.JsonDebugErrorHandler
import org.http4s.client.middleware.*
import org.http4s.client.*

/**
  * Some generic BlazeClient builders, no real uue-case or value added specific to OAuth. TODO: Clean this up, and makr oauth only ones
  * private to oauth package. Woud be nice to make a decent logger based on content type of the entity too.
  */
object OAuthClient {

  val logAction: Some[String => IO[Unit]] = Some((s: String) => IO(scribe.info(s"RQRS: $s")))

  val fullLogger: Client[IO] => Client[IO] = Logger(
    logHeaders = true,
    logBody = true,
    redactHeadersWhen = _ => false,
    logAction = logAction
  )

  /** Simple HTTP4S Client with no middleware */
  val builder: EmberClientBuilder[IO]        = EmberClientBuilder.default[IO]
  val simpleClient: Resource[IO, Client[IO]] = builder.build

  val clientR: Resource[IO, Client[IO]] = simpleClient

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
      base  <- simpleClient
      logged = Logger(logHeaders = true, logBody = true, redactHeadersWhen = _ => false, logAction = logAction)(base)
    } yield logged
  }

  /**
    * Signing Client that follows redirects with no logging. Add your own or build your own from scratch, see OAuthStatusSigner middleware
    */
  def signingClient(oauthSesssion: OAuthSessionData): Resource[IO, Client[IO]] = {
    for {
      base    <- simpleClient
      signing  = OAuthStaticSigner(oauthSesssion)(base)
      redirect = FollowRedirect(3)(signing)
    } yield redirect
  }

  def signingDebugClient(oauthSesssion: OAuthSessionData): Resource[IO, Client[IO]] = {
    for {
      base    <- simpleClient
      logging  = Logger(logHeaders = true, logBody = true, redactHeadersWhen = _ => false, logAction = logAction)(base)
      signing  = OAuthStaticSigner(oauthSesssion)(logging)
      redirect = FollowRedirect(3)(signing)
      cookies <- Resource.pure(CookieJar.impl(redirect))

    } yield redirect

  }

}
