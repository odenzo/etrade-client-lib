package com.odenzo.etrade.oauth

import cats.effect.*
import cats.effect.std.Hotswap
import cats.syntax.all.*
import com.odenzo.etrade.api.models.OAuthSessionData
import org.http4s.client.Client
import org.http4s.{Headers, Request, Response}
import org.typelevel.ci.CIString

/**
  * Simple Middleware for Signing Requests, based on the RequestLogger middlewarequests As They Are Processed Unfortunately this has static
  * OAuthSesssionData but good enough for my use-case. Scope down to whatever is needed to sign request and refresh token. Taglessify I
  * guess there is no reason not to use "ServerSide" vault
  */
object OAuthStaticSigner {

  private def patchHeader(config: OAuthSessionData)(rq: Request[IO]): IO[Request[IO]] = {
    config.accessToken match
      case Some(accessToken) => ClientOAuth.sign(rq, accessToken, config.config.consumer)
      case None              => IO(scribe.warn("No Access Token to SIGN Request - Not Signing")).as(rq)
  }

//  def apply[IO](
//      session: OAuthSessionData
//  )(client: Client[IO]): Client[IO] = {
//    // Request[F] => Resource[F,Response[F]]
//    Client[IO] { (rq: Request[IO]) => Resource.eval(patchHeader(session)(rq)).flatMap(modRq => client.run(modRq)) }
//
//  }

  def apply(session: OAuthSessionData)(client: Client[IO]): Client[IO] = {
    scribe.info(s"Contructing Signer with $session")
    def fn(rq: Request[IO]): Resource[IO, Response[IO]] = {
      scribe.info(s"Signing Middleware Function with Session Access Token: ${session.accessToken}")
      val resourced: Resource[IO, Request[IO]] = Resource.eval(patchHeader(session)(rq))
      val response: Resource[IO, Response[IO]] = resourced.flatMap { (nrq: Request[IO]) =>
        scribe.info(s"OH, doing a client run on Sifned $nrq")
        val firstRs: Resource[IO, Response[IO]] = client.run(nrq)
        scribe.info("RUN Completed into resource.")
        firstRs
      }

      response
    }

    Client.apply(fn)
  }

}
