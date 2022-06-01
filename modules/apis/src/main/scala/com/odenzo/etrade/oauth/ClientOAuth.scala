package com.odenzo.etrade.oauth

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.kernel.Outcome.{Canceled, Errored, Succeeded}
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.api.models.{OAuthConfig, OAuthSessionData}
import com.odenzo.etrade.models.utils.OPrint.oprint
import fs2.concurrent.SignallingRef
import org.http4s.Uri.*
import org.http4s.client.oauth1.ProtocolParameter.Verifier
import org.http4s.client.{Client, oauth1}
import org.http4s.client.oauth1.{Consumer, ProtocolParameter, Token}
import org.http4s.syntax.literals.uri
import org.http4s.{HttpRoutes, ParseFailure, Request, Uri, UrlForm}

import java.util.UUID
import scala.concurrent.duration.*

/**
  * This i an OAuth system that doesn't receive a callback with tokens, instead it needs theuer to cut and paste in the verifier 'keyword'.
  * Useful for both backends programs (I guess) but more so for WebBrowser applications. Peronally I make a system that supports a front-end
  * webapp but uses a backend to supply the accesss tokens (or sometimes just have the backend expose APIs to the front-end. Thats because I
  * mainly store stuff in DB etc which requires back-end. So, this is not really implemented.
  *   - TODO: A few more client accessible things like refreshing the token etc should be put back here, even though don't really use them.
  *   - See Also: https://developer.etrade.com/getting-started/developer-guides
  */
object ClientOAuth extends OAuthHelpers {

  /** Given the callback string with verifier extract it. This is the cut/paste from callback window in same domain. */
  def extractToken(url: String): Either[Throwable, Token] = {
    Uri
      .fromString(url)
      .flatMap { uri =>
        val params             = uri.query.params
        val res: Option[Token] = (params.get("oauth_token"), params.get("oauth_token_secret")).mapN { case (token, secret) =>
          Token(token, secret = secret)
        }
        res.toRight(Throwable("auth_token and oauth_token_sercet not found"))
      }
  }

  /** Once you run this, you have like 2 minutes to call complete with the verifier and the app token returned. */
  def getRequestToken(config: OAuthConfig)(using client: Client[IO]): IO[Token] = requestToken(config.oauthUrl, uri"oob", config.consumer)

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
    * This completes the login process, given all the authentication information required in returned OAuthSessionData.
    *   - When using the OAuth WebServer to handle the callback this is handled automatically.
    */
  def complete(config: OAuthConfig, verifier: String, rqToken: Token)(using client: Client[IO]): IO[OAuthSessionData] = {
    for {
      access <- getAccessToken(verifier, rqToken, config)
      _       = IO(scribe.info(s"Got ACCESS Token $access"))
      session = OAuthSessionData(accessToken = access.some, rqToken = rqToken, config)
    } yield session
  }

  /**
    * Step 1 Get a RequestToken for new login, this is short-lived, 5 minutes. This will do all the signing (with Consumer Token) to get a
    * Request A Token.
    *
    * @param Client
    *   has no special requirements and requests are manually signed. Good to have cookies enabled I think
    */
  def requestToken(baseUri: Uri, callback: Uri, consumer: Consumer)(using client: Client[IO]): IO[Token] = {
    genericSign(Request[IO](uri = baseUri / "request_token"), consumer).flatMap { rq =>
      client
        .run(rq)
        .use { rs =>
          for {
            form  <- rs.as[UrlForm]
            _     <- IO.raiseWhen(form.getFirst("oauth_callback_confirmed").isEmpty)(Throwable(s"No oauth_callback_confirmed"))
            token <- extractToken(form)
          } yield token
        }
    }
  }

  /** Callbacks gives us verifier and auth_token, this uses verifier to get access tokewn */
  def getAccessToken(verifier: String, rqToken: Token, config: OAuthConfig)(using client: Client[IO]): IO[Token] = {
    genericSign(
      rq = Request[IO](uri = config.oauthUrl / "access_token"),
      consumerKeys = config.consumer,
      rqToken.some,
      verifier.some
    ).flatMap { req =>
      client.run(req).use { rs => rs.as[UrlForm].flatMap(extractToken).flatTap(t => IO(scribe.warn(s"Got Access Token $t"))) }
    }
  }

  def renewAccessToken(session: OAuthSessionData)(using client: Client[IO]): IO[Token] = {
    genericSign(
      rq = Request[IO](uri = session.config.oauthUrl / "renew_access_token"),
      session.config.consumer,
      session.rqToken.some
    ).flatMap { rq => client.run(rq).use { rs => rs.as[UrlForm].flatMap(extractToken) } }
  }

  /**
    * Does a refresh/keep-alive with given session request and access tokens. If successful can keep using, on failure generally will need a
    * new request token and relogin
    */
  def refreshAccessToken(session: OAuthSessionData)(using client: Client[IO]): IO[Unit] = {
    for {
      rq <- genericSign(
              Request[IO](uri = session.config.oauthUrl / "renew_access_token"),
              session.config.consumer,
              session.accessToken
            )
      rs <- client.expect[String](rq)
      _  <- IO.raiseWhen(rs != "Access Token has been renewed")(Throwable(s"Invalid Refresh Access Rs: $rs"))
    } yield ()
  }

  /** This can be used to sign a request with the given access token. There is also a signing client that will do it automatically. */
  def sign(rq: Request[IO], accessToken: Token, consumerKeys: Consumer): IO[Request[IO]] = {
    oauth1.signRequest[IO](
      req = rq,
      consumer = ProtocolParameter.Consumer(consumerKeys.key, consumerKeys.secret),
      token = ProtocolParameter.Token(accessToken.value, accessToken.secret).some,
      realm = None,
      signatureMethod = ProtocolParameter.SignatureMethod(),
      timestampGenerator = ts,
      nonceGenerator = nonce
    )
  }

  /** This can be used to sign a request with the given access token. There is also a signing client that will do it automatically. */
  def genericSign(rq: Request[IO], consumerKeys: Consumer, token: Option[Token] = None, verify: Option[String] = None): IO[Request[IO]] =
    oauth1.signRequest[IO](
      req = rq,
      consumer = ProtocolParameter.Consumer(consumerKeys.key, consumerKeys.secret),
      token = token.map(tok => ProtocolParameter.Token(tok.value, tok.secret)),
      realm = None,
      verifier = verify.map(s => Verifier.apply(s)),
      // For etrade this is always 'oob' not the real callback *EXCEPT* maybe getting the initiql request token
      callback = ProtocolParameter.Callback("oob").some,
      signatureMethod = ProtocolParameter.SignatureMethod(),
      timestampGenerator = ts,
      nonceGenerator = nonce
    )

}
