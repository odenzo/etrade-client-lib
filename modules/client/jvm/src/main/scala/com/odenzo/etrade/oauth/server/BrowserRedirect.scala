package com.odenzo.etrade.oauth.server

import cats.effect.IO
import com.odenzo.etrade.api.models.OAuthConfig
import org.http4s.Uri
import org.http4s.Uri.Fragment
import org.http4s.client.oauth1.{Consumer, Token}
import os.CommandResult

/** A function that opens the browser for user login, platform dependant. Should raise error on failure */
type BrowserRedirectFn = (uri: Uri) => IO[Unit]

/**
  * # Markup Style
  *   - Called from Initiate Login Authentication Action to open a browser to login to etrade. The login will result in a callback url on
  *     our web server being redirected to (a localhost). This should be customized for Linux and Windows based on env var.
  */
object BrowserRedirect {

  /** MacOS Safari Opening */
  def openMacOsSafariBrowser(uri: Uri): IO[Unit] =
    IO {
      val urlTxt = uri.renderString
      os.proc("open", "-a", "Safari", urlTxt).call()
    }.void

  def openMacOsEdge(uri: Uri): IO[Unit] =
    IO {
      val urlTxt = uri.renderString
      os.proc("open", "-a", "Microsoft Edge", urlTxt).call()
    }.void

  def noop(uri: Uri): IO[Unit] = IO.unit
  // Hmm, should try and write a automated script and see if we can mimic a real browser to automate it.
  // maybe curl, maybe just a HTTP4S client?

  /**
    * This is duplicated due to JS/JVM splitting. One route is for oauth to open a browser itself. Another route is have UI open the
    * browser, and callback goes to our custom server
    * @param url
    *   The base URL to redirect to, this is the e-trade page.
    * @param appConsumerKeys
    *   We need to pass the registered Consumer Key (not Secret) to validate which Application is doing the redirect.
    * @param appToken
    *   I call this the RequestToken sometimes. We get after authenticating the application to etrade
    * @return
    */
  def buildRedirectUrl(config: OAuthConfig, appToken: Token): Uri = {
    val url: Uri                 = config.redirectUrl
    val appConsumerKey: Fragment = config.consumer.key
    url.withQueryParam("key", appConsumerKey).withQueryParam("token", appToken.value)
  }

}
