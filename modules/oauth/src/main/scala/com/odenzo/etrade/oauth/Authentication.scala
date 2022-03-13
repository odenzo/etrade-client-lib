package com.odenzo.etrade.oauth

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.effect.syntax.all.*
import cats.effect.{Fiber, FiberIO, IO}
import cats.syntax.all.*
import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.oauth.config.OAuthConfig
import com.odenzo.etrade.oauth.utils.OAuthUtils
import org.http4s.*
import org.http4s.CacheDirective.public
import org.http4s.client.oauth1.ProtocolParameter.*
import org.http4s.client.oauth1.{Consumer, Token, *}
import org.http4s.client.{Client, oauth1}
import org.http4s.headers.Location

import java.time.Instant
import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

/** Manages the e-trade oauth callback and manul pasting authentication. */
object Authentication extends OAuthUtils {

  private val nonce: IO[Nonce]  = IO.delay(Nonce(UUID.randomUUID().toString))
  private val ts: IO[Timestamp] = IO.delay(Timestamp(Instant.now().getEpochSecond.toString))

  // TODO: Loosing type info on list size, new tuples should work better?
  private def getFormVar(form: UrlForm, field: String*): ValidatedNec[String, List[String]] = field
    .toList
    .traverse { n => form.getFirst(n).fold(s"Form Var $n not found".invalidNec)(v => v.validNec) }

  /** Move to validation for better error message */
  private def extractToken(form: UrlForm): IO[Token] = IO(getFormVar(form, "oauth_token", "oauth_token_secret")).flatMap {
    case Valid(List(token, secret)) => Token(token, secret).pure
    case Invalid(msg)               => IO.raiseError(Exception(s"Trouble Extract Auth Tokens: ${msg.toList.mkString("\n")}"))
    case Valid(l: List[String])     => IO.raiseError(Exception(s"List size ${l.size} != 2 for parameters"))
  }

  def sign(rq: Request[IO], accessToken: Token, consumerKeys: Consumer): IO[Request[IO]] = oauth1.signRequest[IO](
    req = rq,
    consumer = ProtocolParameter.Consumer(consumerKeys.key, consumerKeys.secret),
    token = ProtocolParameter.Token(accessToken.value, accessToken.secret).some,
    realm = None,
    signatureMethod = ProtocolParameter.SignatureMethod(),
    timestampGenerator = ts,
    nonceGenerator = nonce
  )

  /**
    * Step 1 Get a RequestToken for new login, this is short-lived, 5 minutes. Note that the HTTP Server for the real callback should be
    * running already.
    */
  def requestToken(baseUri: Uri, callback: Uri, consumer: Consumer)(using client: Client[IO]): IO[Token] = {
    scribe.info(s"Getting Request Token $baseUri aith Callback $callback")
    val rqTokenUrl: Request[IO]   = Request[IO](uri = baseUri / "oauth" / "request_token")
    val signedRq: IO[Request[IO]] = oauth1.signRequest[IO](
      req = rqTokenUrl,
      consumer = ProtocolParameter.Consumer(consumer.key, consumer.secret), // Application Consumer
      callback = ProtocolParameter.Callback(callback.toString).some,        // For etrade this is always 'oob' not the real callback
      token = Option.empty[ProtocolParameter.Token],
      realm = Option.empty[Realm],
      verifier = Option.empty[Verifier],
      nonceGenerator = nonce,
      timestampGenerator = ts
    )

    def handleResponse(rs: Response[IO]): IO[Token] =
      for {
        form  <- rs.as[UrlForm]
        _      = dumpResponse(rs)
        _     <- IO.raiseWhen(form.getFirst("oauth_callback_confirmed").isEmpty)(Throwable(s"No oauth_callback_confirmed"))
        token <- extractToken(form)
      } yield token

    signedRq
      .flatTap(rq => IO(scribe.info(s"Initial Call to Get Request Token\n: ${dumpRequest(rq)}")))
      .flatMap(rq => client.run(rq).use(handleResponse))
  }

  /** Callbacks gives us verifier and auth_token, this uses verifier to get access tokewn (with no auth_token used!?) */
  def getAccessToken(verifier: String, rqToken: Token, authToken: String, config: OAuthConfig)(using client: Client[IO]): IO[Token] = {

    val rq: IO[Request[IO]] = oauth1.signRequest[IO](
      req = Request[IO](uri = config.oauthUrl / "oauth" / "access_token"),
      consumer = ProtocolParameter.Consumer(config.consumer.key, config.consumer.secret),
      verifier = Verifier(verifier).some,
      token = ProtocolParameter.Token(rqToken.value, rqToken.secret).some,
      callback = Option.empty[ProtocolParameter.Callback],
      timestampGenerator = ts,
      nonceGenerator = nonce,
      realm = Option.empty[Realm]
    )

    def handleResponse(rs: Response[IO]): IO[Token] = rs.as[UrlForm].flatMap(extractToken)

    rq.flatMap(req => client.run(req).use(handleResponse))

  }

  def renewAccessToken(session: OAuthSessionData)(using client: Client[IO]): IO[Token] = {
    val config              = session.config
    val rq: IO[Request[IO]] = oauth1.signRequest[IO](
      req = Request[IO](uri = config.oauthUrl / "oauth" / "renew_access_token"),
      consumer = ProtocolParameter.Consumer(config.consumer.key, config.consumer.secret),
      token = ProtocolParameter.Token(session.rqToken.value, session.rqToken.secret).some, // Says Consumer Request, but thinks its auth?
      callback = Option.empty[ProtocolParameter.Callback],
      timestampGenerator = ts,
      nonceGenerator = nonce,
      realm = Option.empty[Realm]
    )

    def handleResponse(rs: Response[IO]): IO[Token] = rs.as[UrlForm].flatMap(extractToken)

    rq.flatMap(rq => client.run(rq).use(handleResponse))
  }

  def refreshAccessToken(config: OAuthConfig, rqToken: Token)(using client: Client[IO]): IO[Token] = {
    val rq: IO[Request[IO]] = oauth1.signRequest[IO](
      req = Request[IO](uri = config.oauthUrl / "oauth" / "renew_access_token"),
      consumer = ProtocolParameter.Consumer(config.consumer.key, config.consumer.secret),
      token = ProtocolParameter.Token(rqToken.value, rqToken.secret).some, // Says Consumer Request, but thinks its auth?
      callback = Option.empty[ProtocolParameter.Callback],
      timestampGenerator = ts,
      nonceGenerator = nonce,
      realm = Option.empty[Realm]
    )

    def handleResponse(rs: Response[IO]): IO[Token] = rs.as[UrlForm].flatMap(extractToken)

    rq.flatMap(rq => client.run(rq).use(handleResponse))
  }

}
