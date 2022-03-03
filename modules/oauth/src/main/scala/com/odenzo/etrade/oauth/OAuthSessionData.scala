package com.odenzo.etrade.oauth

import cats.conversions.all.autoWidenFunctor
import cats.effect.IO
import cats.implicits.catsSyntaxOptionId

import com.odenzo.etrade.oauth.config.OAuthConfig
import org.http4s.Request
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
    id: UUID,
    accessToken: Option[Token],
    authToken: String,
    verifier: String,    // Transient but I am lazy
    config: OAuthConfig, // Just in case we have some different clients (sandbox / prod) using same system
    created: Instant = Instant.now()
) {

  def setAccessToken(at: Token): OAuthSessionData = this.copy(accessToken = at.some)

  def clearAccessToken(): OAuthSessionData = this.copy(accessToken = None)

}

object OAuthSessionData {}
