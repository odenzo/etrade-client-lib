package com.odenzo.etrade.oauth

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.api.Authentication
import com.odenzo.etrade.base.OPrint.oprint
import com.odenzo.etrade.api.models.{OAuthConfig, OAuthSessionData}
import com.odenzo.etrade.oauth.client.middleware.OAuthClientMiddleware
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
object OAuthLogic {

  /** Once you run this, you have like 2 minutes to call complete with the verifier and the app token returned. */
  def initiateLogin(config: OAuthConfig)(using client: Client[IO]): IO[Token] = Authentication
    .requestToken(config.oauthUrl, uri"oob", config.consumer)

  /**
    * @param url
    *   The base URL to redirect to, this is the e-trade page.
    * @param appConsumerKeys
    *   We need to pass the registered Consumer Key (not Secret) to validate which Application is doing the redirect.
    * @param appToken
    *   I call this the RequestToken sometimes. We get after authenticating the application to etrade
    * @return
    */
  def constructFullRedirectUrlForNoCallback(config: OAuthConfig, appToken: Token): Uri = {
    val url: Uri                 = config.redirectUrl
    val appConsumerKey: Fragment = config.consumer.key
    url.withQueryParam("key", appConsumerKey).withQueryParam("token", appToken.value)
  }

  /**
    * This completes the login process, given all the authentication information required in returned OAuthSessionData. This can be used to
    * create your client with authentication middleware via createClient ..
    */
  def complete(config: OAuthConfig, verifier: String, rqToken: Token)(using client: Client[IO]): IO[OAuthSessionData] = {
    for {
      access <- Authentication.getAccessToken(verifier, rqToken, config)
      _       = IO(scribe.info(s"Got ACCESS Token $access"))
      session = OAuthSessionData(accessToken = access, rqToken = rqToken, config)
    } yield session
  }
//  def enhanceClient()                                                                                                 = {
//
//    /**
//      * Given an e-trade login verification code, construct a HTTP4S Client Resource that has middleware to automatically set the access
//      * token.
//      * @return
//      *   Suspended client, you should run and then `use` this right away.
//      */
//    def provideClient(
//        verificationCode: String,
//        appToken: Token,
//        withDebugging: Boolean,
//        config: ETradeConfig
//    ): IO[Resource[IO, Client[IO]]] = {
//      val session: IO[OAuthSessionData]         = OAuthLogic.complete(config.asOAuthConfig, verifier = verificationCode, appToken)
//      val clientR: IO[Resource[IO, Client[IO]]] = session.map(os => OAuthLogic.createClient(os, withDebugging))
//      clientR
//    }
//  }
}
