package com.odenzo.etrade.client.models

import org.http4s.Uri
import org.http4s.client.oauth1.Consumer
import org.http4s.syntax.literals.uri

/**
  * e-trade specific configuration. This is used (contextually) in etrade-client module and etrade-oauth module to construct and sign
  * Requests
  *
  * @param oauthUrl
  *   The full URL call to authorizeaseUri, e.g. https://api.etrade.com/oauth/ This is the same wether using the sandbox or live account.
  * @param apiUrl
  *   api.etrade.com or apisb.etrade.com for the non-auth api calls. No /v1 included. Thid depends on if you are using sandbox or live.
  * @param consumer
  *   Consumer keys allocated to our application. These are "secret" and should be loading from vault or environment etc. Not in Git.
  * @param callbackUrl
  *   The local url etrade will call, and we provide the Web Server to monitor. This must be registered with e-trade for your application.
  *   As it is a single user application, if not you probably know the drill to make a commercial end-usser app.
  */
case class OAuthConfig(
    apiUrl: Uri,
    consumer: Consumer,
    callbackUrl: Uri = uri"http://localhost:5555",
    redirectUrl: Uri
) {
  def oauthUrl: Uri = apiUrl / "oauth"
}
