package com.odenzo.etrade.api.models

import org.http4s.client.oauth1.{Consumer, Token}
import cats.conversions.all.autoWidenFunctor
import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import org.http4s.{Request, Uri}
import org.http4s.Uri.*

import org.http4s.implicits.uri
import org.http4s.{Request, Uri}
import io.circe.*
import java.time.Instant
import java.util.UUID
import org.http4s.circe.*

/**
  * SOAuthSession data needs to store information that the e-trade client will use to make calls to e-trade from exisintg Requests.
  *   - TODO: Probably better to have a facade for this to JSON since it goes between apps, but no Chimney and I am lazy now
  */
case class OAuthSessionData(
    accessToken: Token,
    rqToken: Token,
    config: OAuthConfig, // Just in case we have some different clients (sandbox / prod) using same system
    created: Instant = Instant.now()
) derives Codec.AsObject {

  def setAccessToken(at: Token): OAuthSessionData = this.copy(accessToken = at)

}

object OAuthSessionData {

  type Contextual[T] = OAuthSessionData ?=> T

  val baseUri: Contextual[Uri]       = summon[OAuthSessionData].config.apiUrl
  val accessToken: Contextual[Token] = summon[OAuthSessionData].accessToken
  // val authToken: Contextual[Option[Token]]   = summon[OAuthSessionData].authToken
  // def token(ctx: OAuthSessionData): Contextual[Token] = ctx.accessToken.getOrElse(throw Throwable("Hissy Fit"))
}
