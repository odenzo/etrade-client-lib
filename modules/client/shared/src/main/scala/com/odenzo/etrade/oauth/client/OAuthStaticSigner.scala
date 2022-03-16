package com.odenzo.etrade.oauth.client

import cats.effect.Ref
import cats.effect.*
import cats.syntax.all.*
import com.odenzo.etrade.client.models.OAuthSessionData
import com.odenzo.etrade.oauth.Authentication
import fs2.*
import org.http4s.{Request, Response}
import org.http4s.client.Client
import org.http4s.internal.Logger as InternalLogger
import org.log4s.getLogger
import org.typelevel.ci.CIString

/**
  * Simple Middleware for Signing Requests, based on the RequestLogger middlewarequests As They Are Processed Unfortunately this has static
  * OAuthSesssionData but good enough for my use-case. Scope down to whatever is needed to sign request and refresh token. Taglessify I
  * guess there is no reason not to use "ServerSide" vault
  */
object OAuthStaticSigner {

  private def patchHeader(config: OAuthSessionData)(rq: Request[IO]): IO[Request[IO]] = Authentication
    .sign(rq, config.accessToken, config.config.consumer)

  def apply(config: OAuthSessionData)(client: Client[IO]): Client[IO] =

    def fn(rq: Request[IO]): Resource[IO, Response[IO]] = {
      val resourced: Resource[IO, Request[IO]] = Resource.eval(patchHeader(config)(rq))
      val response: Resource[IO, Response[IO]] = resourced.flatMap((nrq: Request[IO]) => client.run(nrq))
      // We could in the future handle authorization failure.
      response
    }

    Client.apply(fn)

}
