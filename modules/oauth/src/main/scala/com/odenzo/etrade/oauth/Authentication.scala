package com.odenzo.etrade.oauth

import cats.effect.{Fiber, FiberIO, IO}
import cats.implicits.{catsSyntaxOptionId, catsSyntaxTuple2Semigroupal}
import com.odenzo.base.OPrint.oprint
import com.odenzo.base.{IOU, OLogging}
import com.odenzo.etrade.client.api.UsingRestClient
import com.odenzo.etrade.client.models.internal.AppConfig
import monocle.Monocle.toAppliedFocusOps
import org.http4s.CacheDirective.public
import org.http4s.client.oauth1.ProtocolParameter.*
import org.http4s.client.oauth1.{Consumer, Token, *}
import org.http4s.client.{Client, oauth1}
import org.http4s.headers.Location
import org.http4s.*

import java.time.Instant
import java.util.UUID
import scala.util.chaining.scalaUtilChainingOps

/** Manages the e-trade oauth callback and manul pasting authentication. */
object Authentication {

  private val nonce: IO[Nonce] = IO.delay(Nonce(UUID.randomUUID().toString))

  private val ts: IO[Timestamp] = IO.delay(Timestamp(Instant.now().getEpochSecond.toString))

  /** Move to validation for better error message */
  private def extractToken(form: UrlForm): IO[Token] = {
    (form.getFirst("oauth_token"), form.getFirst("oauth_token_secret")).mapN(Token.apply).pipe(IOU.required("OAuth Token"))
  }

  /** General signing of a request, e.g. getAccounts (maybe for oauth too) */
  def signRq(rq: Request[IO], session: OAuthSession): IO[Request[IO]] = {
    val pConsumer = ProtocolParameter.Consumer(session.config.consumer.key, session.config.consumer.secret)
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

  /** Some prework and returns a URL the user must open in browser */
  def initiateLogin(config: AppConfig, user: String)(implicit client: Client[IO]): IO[Uri] = {
    for {
      rqToken <- requestToken(config.baseUri, config.callbackUrl, config.consumer)
      _        = scribe.info(s"Request Token: ${rqToken}")
      redirect = config.authUrl.withQueryParam("key", config.consumer.key).withQueryParam("token", rqToken.value)
      session  = ETradeSession(None, None, user, rqToken, config)
      _       <- ETradeSessionManager.storeUpdatedSession(session)
    } yield redirect
  }

  /**
    * Step 1 Get a RequestToken for new login, this is short-lived, 5 minutes. Creates its own HTTP4s Client. @return consumer outh token
    * and consumer oauth token secret . This is a signed oauth request. Note here it is the "application" signing the request, not the
    * logged in user.
    */
  def requestToken(baseUri: Uri, callback: Uri, consumer: Consumer)(implicit client: Client[IO]): IO[Token] = {
    scribe.info(s"Getting Request Token $baseUri")
    val rqTokenUrl: Request[IO] = Request[IO](uri = baseUri / "oauth" / "request_token")
    val signedRq                = oauth1.signRequest[IO](
      req = rqTokenUrl,
      consumer = ProtocolParameter.Consumer(consumer.key, consumer.secret),
      callback = ProtocolParameter.Callback(callback.renderString).some,
      token = Option.empty[ProtocolParameter.Token],
      realm = Option.empty[Realm],
      // signatureMethod = HmacSha1, // HmacSha256
      timestampGenerator = ts, // IO(ProtocolParameter.Timestamp(Instant.now().toString)),
      verifier = Option.empty[Verifier],
      nonceGenerator = nonce
    )
    scribe.debug(s"Signed Call: $signedRq")
    call(signedRq) {
      (rs: Response[IO]) =>
        scribe.info(s"Call REsponse: $rs")
        for {
          form  <- rs.as[UrlForm]
          _     <- form.getFirst("oauth_callback_confirmed").pipe(IOU.required(s"Callback_confirmed"))
          token <- extractToken(form)
        } yield token
    }
  }

  /**
    * After we handle the callback we can shutdown our webserver. But we need the fiber to join on which is in an IO stuck in resource block
    * in main
    */
  def handleCallback(user: String, verifier: String, authToken: String)(implicit client: Client[IO]): IO[ETradeSession] = {
    for {
      _              <- IO(scribe.info(s"oauth callback [$verifier] / [$authToken]"))
      session        <- ETradeSessionManager.getSession(user)
      accessToken    <- Authentication.getAccessToken(verifier, authToken, session)
      _               = scribe.info(s"Returned Access Token: ${accessToken}")
      _               = scribe.info(s"Session Pre-Access: ${oprint(session)}")
      authedSession   = session.focus(_.accessToken).replace(accessToken.some).focus(_.verifier).replace(verifier.some)
      _              <- ETradeSessionManager.storeUpdatedSession(authedSession)
      updatedSession <- ETradeSessionManager.getSession(user)
      _               = scribe.info(s"Session Post Callback for $user ==  ${oprint(updatedSession)}")
    } yield updatedSession
  }

  /**
    * When doing on oauthcallback this is not needed, token returned directly is access token. Note, this is a special signing to get the
    * access token, which is then used to sign all "user" requests.
    */
  def getAccessToken(verifier: String, callbackToken: String, session: ETradeSession)(implicit client: Client[IO]): IO[Token] = {
    val baseRq = Request[IO](uri = session.config.baseUri / "oauth" / "access_token")
    val rq     = oauth1.signRequest[IO](
      baseRq,
      ProtocolParameter.Consumer(session.config.consumer.key, session.config.consumer.secret),
      verifier = Verifier(verifier).some,
      token = ProtocolParameter.Token(session.reqToken.value, session.reqToken.secret).some,
      callback = Option.empty[ProtocolParameter.Callback],
      timestampGenerator = ts,
      nonceGenerator = nonce,
      realm = Option.empty[Realm]
    )

    this.call(rq)(rs => rs.as[UrlForm].flatMap(extractToken))
  }

  /**
    * I am not sure whose token it knows to renew, except I have single user token so thats easy enough! Probably going to need a new
    * request token first
    */
  def renewAccessToken(session: ETradeSession)(implicit client: Client[IO]): IO[Token] = {
    val config   = session.config
    val signedRq = signRq(Request[IO](uri = config.baseUri / "oauth" / "renew_access_token"), session)

    call(signedRq) { rs => rs.as[UrlForm].flatMap(extractToken) }
  }

  /** Revokes access token, internally returns token but we void, errors in IO */
  def revokeAccessToken(session: ETradeSession)(implicit client: Client[IO]): IO[Unit] = {
    val baseRq   = Request[IO](uri = session.config.baseUri / "oauth" / "revoke_access_token")
    val signedRq = signRq(baseRq, session)

    call(signedRq) { (rs: Response[IO]) => rs.as[UrlForm].flatMap(extractToken) }.void
  }

}
