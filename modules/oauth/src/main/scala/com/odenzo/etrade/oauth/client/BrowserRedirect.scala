package com.odenzo.etrade.oauth.client

import cats.effect.IO
import org.http4s.Uri
import org.http4s.client.oauth1.{Consumer, Token}
import org.http4s.*
import org.http4s.implicits.*
import org.http4s.QueryParameterValue.{given, *}
import org.http4s.QueryParameterKey.*
import org.http4s.QueryParameterKey.*
import os.CommandResult

/**
  * Called from Initiate Login Authentication Action to open a browser to login to etrade. The login will result in a callback url on our
  * web server being redirected to (a localhost). This should be customized for Linux and Windows based on env var.
  */
object BrowserRedirect {
//https://us.etrade.com/e/t/etws/authorize?oauth_consumer_key=306e055f434a852ff3d28b8108c05f8b&oauth_token=rpotvi4McBrR4U7gpWMtvCqpXFpPQizl9anQ0nXZ5Cw%3D
  def openLoginBrowser(uri: Uri): IO[CommandResult] = IO {
    val urlTxt = uri.renderString
    scribe.info(s"Opening Brower to: $urlTxt")
    os.proc("open", "-a", "Safari", urlTxt).call()
  }

  def redirectToETradeAuthorizationPage(url: Uri, appConsumerKeys: Consumer, appToken: Token): IO[CommandResult] = {
    val dest = url
      .withQueryParam("key", appConsumerKeys.key)
      .withQueryParam("token", appToken.value)
    openLoginBrowser(dest)
  }
}
