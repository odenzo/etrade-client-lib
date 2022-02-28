package com.odenzo.etrade.oauth

import cats.conversions.all.autoWidenFunctor
import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import com.odenzo.etrade.client.models.internal.AppConfig
import org.http4s.Request
import org.http4s.client.oauth1.Token

import java.time.Instant

/**
  * State that tracks the OAuth Session, further scoped down to a single session per run rather than dealing with multiple logged in users
  * anymore. (So essentially its almost a singleton, but we may refresh this so its actually mutable session state) We will use scalacache
  * to deal with it, since thats what the previous full system used for sessions.
  */
case class OAuthSession(
    accessToken: Option[Token],
    verifier: Option[String],
    user: String,
    reqToken: Token,
    config: AppConfig,
    created: Instant = Instant.now()
) {

  def setAccessToken(at: Token): OAuthSession = this.copy(accessToken = at.some)

  def clearAccessToken(): OAuthSession = this.copy(accessToken = None)

}
