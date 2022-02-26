package com.odenzo.etrade.oauth

import com.odenzo.etrade.client.authentication.Authentication
import com.odenzo.etrade.client.models.internal.AppConfig
import org.http4s.Uri
import org.http4s.blaze.http.Url
import os.CommandResult

/**
  * Called from Initiate Login Authentication Action to open a browser to login to etrade. The login will result in a callback url on our
  * web server being redirected to (a localhost)
  */
object BrowserRedirect {

  def openLoginBrowser(uri: Uri): CommandResult = {
    os.proc("open", "-a", "Microsoft Edge", uri.renderString).call()
  }
}
