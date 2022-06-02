package com.odenzo.etrade.oauth

import cats.conversions.all.autoWidenFunctor
import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import io.circe.*
import org.http4s.Uri.*
import org.http4s.circe.*
import org.http4s.client.oauth1.{Consumer, Token}
import org.http4s.implicits.uri
import org.http4s.{Request, Uri}

import java.time.Instant
import java.util.UUID

/** Session Data determines both URL endpoints and the signing of packets */
case class OAuthSessionData(
    accessToken: Option[Token],
    rqToken: Token,
    config: OAuthConfig, // Just in case we have some different clients (sandbox / prod) using same system
    created: Instant = Instant.now(),
    maybeUrl: Option[Uri] = None
) derives Codec.AsObject {

  def setAccessToken(at: Token): OAuthSessionData = this.copy(accessToken = at.some)

}

object OAuthSessionData {

  type Contextual[T] = OAuthSessionData ?=> T

  val baseUri: Contextual[Uri]               = summon[OAuthSessionData].config.apiUrl
  val accessToken: Contextual[Option[Token]] = summon[OAuthSessionData].accessToken
  // val authToken: Contextual[Option[Token]]   = summon[OAuthSessionData].authToken
  // def token(ctx: OAuthSessionData): Contextual[Token] = ctx.accessToken.getOrElse(throw Throwable("Hissy Fit"))
}
