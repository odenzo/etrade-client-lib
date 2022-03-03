package com.odenzo.etrade.oauth.config

import org.http4s.Uri
import org.http4s.client.oauth1.Consumer

/**
  * This is fairly e-trade specific since we send oob as callback, and etrade is configured to call localhost
  *
  * @param oauthUrl
  *   The full URL call to authorizeaseUri, e.g. https://api.etrade.com/oauth/request_token the o
  * @param apiUrl
  *   api.etrade.com or apisb.etrade.com for the non-auth api calls. No /v1 included
  * @param consumer
  *   Consumer keys allocated to our application
  * @param callbackUrl
  *   The local url etrade will call, and we provide the Web Server to monitor. This must be registered with e-trade.
  */
case class OAuthConfig(oauthUrl: Uri, apiUrl: Uri, consumer: Consumer, callbackUrl: Uri, redirectUrl: Uri)
