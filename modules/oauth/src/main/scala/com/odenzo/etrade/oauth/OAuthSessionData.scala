package com.odenzo.etrade.oauth

import cats.conversions.all.autoWidenFunctor
import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import com.odenzo.etrade.oauth.config.OAuthConfig
import org.http4s.{Request, Uri}
import org.http4s.Uri.*
import org.http4s.client.oauth1.{Consumer, Token}
import org.http4s.implicits.uri

import java.time.Instant
import java.util.UUID

/**
  * State that tracks the OAuth Session, further scoped down to a single session per run rather than dealing with multiple logged in users
  * anymore. (So essentially its almost a singleton, but we may refresh this so its actually mutable session state) We will use scalacache
  * to deal with it, since thats what the previous full system used for sessions. Should parameterize key and make a trait too
  */
case class OAuthSessionData(
    accessToken: Token,
    rqToken: Token,
    config: OAuthConfig, // Just in case we have some different clients (sandbox / prod) using same system
    created: Instant = Instant.now()
) {

  def setAccessToken(at: Token): OAuthSessionData = this.copy(accessToken = at)

}

object OAuthSessionData {

  // One use of context function. Kinda readble becauea using  make given in a function
  // Let see exactly what kind of imports are needed.
  // baseUri looks like a magic variable once imported :-)
  // and baeUri(ctx) I may as well put in the OAuthSessionData().baseUri as a facade.
  // TODO: So, we need to make a specific type for BaseUri and AccessToken I think.
  type Contextual[T] = OAuthSessionData ?=> T

  val baseUri: Contextual[Uri]       = summon[OAuthSessionData].config.apiUrl
  val accessToken: Contextual[Token] = summon[OAuthSessionData].accessToken
  // val authToken: Contextual[Option[Token]]   = summon[OAuthSessionData].authToken
  // def token(ctx: OAuthSessionData): Contextual[Token] = ctx.accessToken.getOrElse(throw Throwable("Hissy Fit"))
}
