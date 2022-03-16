package com.odenzo.etrade.oauth

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.base.OPrint.oprint
import com.odenzo.etrade.client.models.{OAuthConfig, OAuthSessionData}
import com.odenzo.etrade.oauth.client.OAuthClient
import com.odenzo.etrade.oauth.Authentication
import fs2.concurrent.SignallingRef
import org.http4s.Uri.*
import org.http4s.client.Client
import org.http4s.client.oauth1.{Consumer, Token}
import org.http4s.syntax.literals.uri
import org.http4s.{HttpRoutes, Uri}

import java.util.UUID
import scala.concurrent.duration.*

/**
  * This i an OAuth system that doesn't receive a callback with tokens, instead it needs theuer to cut and paste in the verifier 'keyword'.
  * Useful for both backends programs (I guess) but more so for WebBrowser applications. Peronally I make a system that supports a front-end
  * webapp but uses a backend to supply the accesss tokens (or sometimes just have the backend expose APIs to the front-end. Thats because I
  * mainly store stuff in DB etc which requires back-end. So, this is not really implemented.
  *
  * See Also: https://developer.etrade.com/getting-started/developer-guides
  */
object OAuthNoCallback {

  /** Once you run this, you have like 2 minutes to call complete with the verifier and the app token returned. */
  def initiateLogin(config: OAuthConfig): IO[(Token, Uri)] =
    for {
      appToken <- requestTokenProg(config)
      url       = constructFullRedirectUrlForNoCallback(config, appToken)
    } yield (appToken, url)

  /**
    * This completes the login process, given all the authentication information required in returned OAuthSessionData. This can be used to
    * create your client with authentication middleware via createClient ..
    */
  def complete(config: OAuthConfig, verifier: String, rqToken: Token): IO[OAuthSessionData] = {
    OAuthClient
      .oauthClient
      .use { client =>
        given Client[IO] = client

        for {
          access <- Authentication.getAccessToken(verifier, rqToken, config)
          _       = IO(scribe.info(s"Got ACCESS Token $access"))
          session = OAuthSessionData(accessToken = access, rqToken = rqToken, config)
        } yield (session)
      }
  }

  /**
    * Creates the client, you can create your own easily, just has to be a signging client. These clients do not keep session data, and in
    * fact do not automatically refresh the access token now. Probably should, or you can use a trickle timer to keep it fresh. Typicallu
    * one client per app as its pooled and multi-thread safe.
    */
  def createClient(session: OAuthSessionData, debugging: Boolean): Resource[IO, Client[IO]] = {
    if debugging
    then OAuthClient.signingDebugClient(session)
    else OAuthClient.signingClient(session)
  }

  private[oauth] def requestTokenProg(config: OAuthConfig): IO[Token] = OAuthClient
    .oauthClient
    .use {
      client => Authentication.requestToken(config.oauthUrl, uri"oob", config.consumer)(using client: Client[IO])
    }

  /**
    * @param url
    *   The base URL to redirect to, this is the e-trade page.
    * @param appConsumerKeys
    *   We need to pass the registered Consumer Key (not Secret) to validate which Application is doing the redirect.
    * @param appToken
    *   I call this the RequestToken sometimes. We get after authenticating the application to etrade
    * @return
    */
  private[oauth] def constructFullRedirectUrlForNoCallback(config: OAuthConfig, appToken: Token): Uri = {
    val url: Uri                 = config.redirectUrl
    val appConsumerKey: Fragment = config.consumer.key
    url.withQueryParam("key", appConsumerKey).withQueryParam("token", appToken.value)
  }
}
