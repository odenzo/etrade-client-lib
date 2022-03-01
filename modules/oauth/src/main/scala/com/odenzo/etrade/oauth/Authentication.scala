package com.odenzo.etrade.oauth

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.effect.{Fiber, FiberIO, IO}
import cats.implicits.*
import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.oauth.config.OAuthConsumerKeys
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

  private def getFormVar(form: UrlForm, field: String*): ValidatedNec[String, List[String]] =
    field.toList.traverse { n => form.getFirst(n).fold(s"Form Var $n not found".invalidNec)(v => v.validNec) }

  /** Move to validation for better error message */
  private def extractToken(form: UrlForm): IO[Token] =
    IO.fromEither(getFormVar(form, "oauth_token", "oauth_token_secret")
      .leftMap(errmsg => Throwable(s"Extract Token Data Missing: ${errmsg.combine}")).toEither)
      .map {
        case List(token, secret) => Token(token, secret)
        case other               => throw Throwable("Programming Error - Bad Types")
      }

  /** General signing of a request, e.g. getAccounts (maybe for oauth too) */
  def signRq(rq: Request[IO], session: OAuthSessionData, oauthConsumerKeys: OAuthConsumerKeys): IO[Request[IO]] = {
    val pConsumer = ProtocolParameter.Consumer(oauthConsumerKeys.oauthConsumerKey, oauthConsumerKeys.consumerSecret)
    val pToken    = session.accessToken.map(t => ProtocolParameter.Token(t.value, t.secret))

    oauth1.signRequest[IO](
      req = rq,
      consumer = pConsumer,
      token = pToken,
      realm = None,
      signatureMethod = ProtocolParameter.SignatureMethod(),
      timestampGenerator = ts,
      nonceGenerator = nonce
    )
  }

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
      timestampGenerator = ts,
      verifier = Option.empty[Verifier],
      nonceGenerator = nonce
    )

    def handleResponse(rs: Response[IO]): IO[Token] =
      scribe.info(s"Call REsponse: $rs")
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

  /**
    * When doing on oauthcallback this is not needed, token returned directly is access token. Note, this is a special signing to get the
    * access token, which is then used to sign all "user" requests.
    */
  def getAccessToken(verifier: String, callbackToken: String, session: OAuthSessionData)(using client: Client[IO]): IO[Token] = {
    val baseRq              = Request[IO](uri = session.config.baseUrl / "v1" / "oauth" / "access_token")
    val rq: IO[Request[IO]] = oauth1.signRequest[IO](
      req = baseRq,
      consumer = ProtocolParameter.Consumer(session.config.consumer.key, session.config.consumer.secret),
      verifier = Verifier(verifier).some,
      token = ProtocolParameter.Token(session.reqToken.value, session.reqToken.secret).some,
      callback = Option.empty[ProtocolParameter.Callback],
      timestampGenerator = ts,
      nonceGenerator = nonce,
      realm = Option.empty[Realm]
    )

    def handleResponse(rs: Response[IO]): IO[Token] = rs.as[UrlForm].flatMap(extractToken)

    rq.flatMap(rq => client.run(rq).use(handleResponse))
  }

//  /**
//    * I am not sure whose token it knows to renew, except I have single user token so thats easy enough! Probably going to need a new
//    * request token first
//    */
//  def renewAccessToken(session: OAuthSessionData)(implicit client: Client[IO]): IO[Token] = {
//    val rq =
//      signRq(Request[IO](uri = session.config.baseUrl / "v1" / "oauth" / "renew_access_token"), session, session.config.consumer)
//
//    def handleResponse(rs: Response[IO]): IO[Token] = rs.as[UrlForm].flatMap(extractToken)
//    rq.flatMap(rq => client.run(rq).use(handleResponse))
//  }
//
//  /** Revokes access token, internally returns token but we void, errors in IO */
//  def revokeAccessToken(session: OAuthSessionData)(implicit client: Client[IO]): IO[Unit] = {
//    val baseRq = Request[IO](uri = session.config.baseUrl / "v1" / "oauth" / "revoke_access_token")
//    val rq     = signRq(baseRq, session, OAuthConsumerKeys("foo", "secret"))
//
//    def handleResponse(rs: Response[IO]): IO[Unit] = rs.as[UrlForm].flatMap(extractToken).void
//    rq.flatMap(rq => client.run(rq).use(handleResponse))
//  }

  def pileofwork() = {
//  accessToken <- Authentication.getAccessToken(verifier, auth_token, session)
//  access       = session.copy(accessToken = accessToken.some)
//  _            = scribe.info(s"Returned Access Token: ${accessToken}")
//  // sessionKey     <- IO(UUID.randomUUID())
//  // authedSession   = .focus(_.accessToken).replace(accessToken.some).focus(_.verifier).replace(verifier.some)
//  // _              <- cache.put(sessionKey, authedSession)
//  done        <- Ok(s"Verifier: $verifier and Auth Token: $auth_token  $accessToken ....")
//  _           <- IO.sleep(5.minutes) *> IO(scribe.info(s"Done Pretending to do callback work")) //
  }
}
