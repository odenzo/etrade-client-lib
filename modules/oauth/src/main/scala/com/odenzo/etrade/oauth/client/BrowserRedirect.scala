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

  def openLoginBrowser(uri: Uri): IO[CommandResult] = IO {
    os.proc("open", "-a", "Microsoft Edge", uri.renderString).call()
  }

  def redirectToETradeAuthorizationPage(url: Uri, appConsumerKeys: Consumer, appToken: Token): IO[CommandResult] = {
    val dest = url
      .withQueryParam("oauth_consumer_key", appConsumerKeys.key)
      .withQueryParam("oauth_token", appToken.value)
    openLoginBrowser(dest)
  }
}
