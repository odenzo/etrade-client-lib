package com.odenzo.etrade.api

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.effect.syntax.all.*
import cats.effect.{Fiber, FiberIO, IO}
import cats.syntax.all.*
import com.odenzo.etrade.api.models.{OAuthConfig, *}
import com.odenzo.etrade.api.utils.OAuthHelpers
import com.odenzo.etrade.base.OPrint.oprint
import org.http4s.*
import org.http4s.CacheDirective.public
import org.http4s.client.oauth1.ProtocolParameter.*
import org.http4s.client.oauth1.{Consumer, Token, *}
import org.http4s.client.{Client, oauth1}
import org.http4s.headers.Location

import java.time.Instant
import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

/** Manages the e-trade oauth callback and manul pasting authentication. Note: These are no going to be called from the Browser client */
object Authentication extends OAuthHelpers {

  /** This can be used to sign a request with the given access token. There is also a signing client that will do it automatically. */
  def sign(rq: Request[IO], accessToken: Token, consumerKeys: Consumer): IO[Request[IO]] = oauth1.signRequest[IO](
    req = rq,
    consumer = ProtocolParameter.Consumer(consumerKeys.key, consumerKeys.secret),
    token = ProtocolParameter.Token(accessToken.value, accessToken.secret).some,
    realm = None,
    signatureMethod = ProtocolParameter.SignatureMethod(),
    timestampGenerator = ts,
    nonceGenerator = nonce
  )

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

  /**
    * Step 1 Get a RequestToken for new login, this is short-lived, 5 minutes. Note that the HTTP Server for the real callback should be
    * running already.
    *
    * @param Client
    *   has no special requirements and requests are manually signed. Good to have cookies enabled I think
    */
  def requestToken(baseUri: Uri, callback: Uri, consumer: Consumer)(using client: Client[IO]): IO[Token] = {

    genericSign(Request[IO](uri = baseUri / "oauth" / "request_token"), consumer).flatMap { rq =>
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

  /** Callbacks gives us verifier and auth_token, this uses verifier to get access tokewn (with no auth_token used!?) */
  def getAccessToken(verifier: String, rqToken: Token, config: OAuthConfig)(using client: Client[IO]): IO[Token] = {
    genericSign(
      rq = Request[IO](uri = config.oauthUrl / "oauth" / "access_token"),
      consumerKeys = config.consumer,
      rqToken.some,
      verifier.some
    ).flatMap { req =>
      client.run(req).use { rs => rs.as[UrlForm].flatMap(extractToken).flatTap(t => IO(scribe.warn(s"Got Access Token $t"))) }
    }
  }

  def renewAccessToken(session: OAuthSessionData)(using client: Client[IO]): IO[Token] = {
    genericSign(
      rq = Request[IO](uri = session.config.oauthUrl / "oauth" / "renew_access_token"),
      session.config.consumer,
      session.rqToken.some
    ).flatMap { rq => client.run(rq).use { rs => rs.as[UrlForm].flatMap(extractToken) } }
  }

  def refreshAccessToken(config: OAuthConfig, rqToken: Token)(using client: Client[IO]): IO[Token] = {
    genericSign(
      Request[IO](uri = config.oauthUrl / "oauth" / "renew_access_token"),
      config.consumer,
      rqToken.some
    ).flatMap { rq => client.run(rq).use(rs => rs.as[UrlForm].flatMap(extractToken)) }
  }

}
