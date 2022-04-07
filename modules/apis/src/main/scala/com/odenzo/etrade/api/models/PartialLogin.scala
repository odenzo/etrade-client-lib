package com.odenzo.etrade.api.models

import cats.effect.IO
import org.http4s.Uri

/**
  * The owner of this is responsible for redirecting user to given webpage while running session in the background to receive the callback.
  * Session will complete on timeout or on successful login. Probably better to run the server in background and return the Deffered or let
  * caller pass in a deferred value. Rethink this to be compatable with all ways of login.
  * @param redirectUrl
  *   Has the URL with consumer and app request token to redirect to.
  * @param session
  *   This is a blocking (deferred) result and should be run concurrently with user redirection to web page
  */
case class PartialLogin(redirectUrl: Uri, session: IO[OAuthSessionData])
