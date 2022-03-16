package com.odenzo.etrade

import cats.effect.{IO, Resource}
import com.odenzo.etrade.client.models.{ETradeConfig, OAuthConfig, OAuthSessionData}
import com.odenzo.etrade.oauth.{Authentication, OAuthNoCallback}
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.oauth1.Token

/**
  * This interface is more cumbersome and requires either the front-end app to redirect browser or the back-end to open browser window and
  * manually cut-paste the verification code.
  */
object ETrade {

  /**
    * This will do a few calls to etrade to calculate parameters for the re-direct URL. On JVM see BrowserRedirect for methods of opening
    * the browser.
    */
  def calculateRedirectUrl(config: ETradeConfig): IO[(Token, Uri)] = OAuthNoCallback.initiateLogin(config.asOAuthConfig)

  /**
    * Given an e-trade login verification code, construct a HTTP4S Client Resource that has middleware to automatically set the access
    * token.
    * @return
    *   Suspended client, you should run and then `use` this right away.
    */
  def provideClient(
      verificationCode: String,
      appToken: Token,
      withDebugging: Boolean,
      config: ETradeConfig
  ): IO[Resource[IO, Client[IO]]] = {
    val session: IO[OAuthSessionData]         = OAuthNoCallback.complete(config.asOAuthConfig, verifier = verificationCode, appToken)
    val clientR: IO[Resource[IO, Client[IO]]] = session.map(os => OAuthNoCallback.createClient(os, withDebugging))
    clientR
  }

}
