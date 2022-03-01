package com.odenzo.etrade.oauth

import cats.conversions.all.autoWidenFunctor
import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import com.odenzo.etrade.oauth.config.OAuthConfig
import org.http4s.Request
import org.http4s.client.oauth1.Token

import java.time.Instant

case class OAuthSession(
    accessToken: Option[Token],
    verifier: Option[String],
    user: String,
    reqToken: Token,
    config: OAuthConfig,
    created: Instant = Instant.now()
) {

  def setAccessToken(at: Token): OAuthSession = this.copy(accessToken = at.some)

  def clearAccessToken(): OAuthSession = this.copy(accessToken = None)

}
