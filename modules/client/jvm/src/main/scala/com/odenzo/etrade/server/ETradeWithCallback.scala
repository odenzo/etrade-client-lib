package com.odenzo.etrade.server

import cats.effect.{IO, Resource}
import com.odenzo.etrade.api.ETradeContext
import com.odenzo.etrade.client.models.ETradeConfig
import com.odenzo.etrade.oauth.client.OAuthClientMiddleware
import com.odenzo.etrade.oauth.server.OAuthCallback
import com.odenzo.etrade.oauth.server.BrowserRedirect
import org.http4s.client.Client

/** This is JVM Only ETrade Client with Callbacks via putting up a local WebServer to catch the E-Trade Callback. */
object ETradeWithCallback {

  /**
    * Produces an IO program then when run begins the etrade authentication uses a web server callback. It returns (now) a Resourse to 'use'
    * that providers the HTTP4S signing enabled client and the ETradeContext. Both of these are generally put in implicit context using
    * 'given'
    */
  def callbackBasedClient(config: ETradeConfig): IO[Resource[IO, (Client[IO], ETradeContext)]] = {
    val context = config.asContext
    val oauth   = OAuthCallback(config.asOAuthConfig) //  setting up
    oauth
      .login(BrowserRedirect.openMacOsEdge)
      .map(session => OAuthClientMiddleware.signingDebugClient(session).map(client => (client, context)))

  }

  /** Hybrid Method where we have a back-end callback server and just want to pass a logging in OAuthSession to front end */

}
