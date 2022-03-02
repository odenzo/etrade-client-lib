package com.odenzo.etrade.oauth

import cats.effect.{Deferred, IO, Resource}
import cats.implicits.*
import com.github.blemale.scaffeine
import com.odenzo.base.OPrint.oprint
import com.odenzo.base.ScribeConfig
import com.odenzo.etrade.models.Account
import com.odenzo.etrade.models.responses.ListAccountsRs
import com.odenzo.etrade.oauth.*
import com.odenzo.etrade.oauth.client.*
import com.odenzo.etrade.oauth.config.OAuthConfig
import org.http4s.Uri
import org.http4s.Uri.{*, given}
import org.http4s.client.Client
import org.http4s.client.oauth1.{Consumer, Token}
import org.http4s.server.Server
import org.http4s.syntax.all.{*, given}

import java.util.UUID
import scala.concurrent.duration.*

class MainTest extends munit.CatsEffectSuite {

  type WebCall[T] = Client[IO] ?=> T

  override val munitTimeout = Duration(10, "minutes")

  ScribeConfig.setupRoot(onlyWarnings = List("org.http4s.blaze"), initialLevel = scribe.Level.Info)

  test("Main") {
    val url: Uri = uri"https://api.etrade.com/"
    val callback = uri"http://localhost:5555/etrade/oauth_callback" // or 8888
    val key      = scala.sys.env("ETRADE_SANDBOX_KEY")
    val secret   = scala.sys.env("ETRADE_SANDBOX_SECRET")
    val config   = OAuthConfig(url, Consumer(key, secret), callback, uri"https://us.etrade.com/e/t/etws/authorize")

    val oauth = OAuth(config)
    scribe.info(s"Running Main Test with Config ${oprint(config)}")

    // Bit goofy as I work through the details to package up into OAuth itself

    oauth.cacheR.use {
      cache =>
        val user = for {
          userId  <- IO(UUID.randomUUID())
          session <- initialLogin(oauth)
          _       <- IO(scribe.info(s"Client, Deffered and Server All Closed: ${session}"))
          _        = cache.put(userId, session)
        } yield userId

        // Seperated out because this is really all we want to require other than OAuthConfig
        user.void
    }

  }

  /**
    * This will start the WebServer, open a browser and wait for user to succesfully login. This is a fibre blocking operation that has a 5
    * minute timeout. The resulting login data is returned in a partially filled OAuthSessionData. (After this login we no longer need the
    * webserver, but to still need to get a access token for regular use)
    */
  def initialLogin(oauth: OAuth): IO[OAuthSessionData] = Deferred[IO, OAuthSessionData].flatMap {
    (returnedData: Deferred[IO, OAuthSessionData]) =>
      OAuthClient.simpleClient.use {
        client =>
          given Client[IO] = client

          Authentication.requestToken(oauth.config.baseUrl, uri"oob", oauth.config.consumer)
            .flatTap(t => IO(scribe.info(s"Got  RequestToken: $t")))
            .flatMap {
              rqToken =>
                val phase1Login: IO[OAuthSessionData] = oauth.serverR(returnedData).use {
                  server =>
                    for {
                      _     <- IO(scribe.info(s"Running Server and Redirecting Browser"))
                      _     <- BrowserRedirect.redirectToETradeAuthorizationPage(oauth.config.redirectUrl, oauth.config.consumer, rqToken)
                      _     <- IO(scribe.info("Waiting on the confirmed data..."))
                      login <- returnedData.get.timeout(2.minutes) // SemVar -- will "block" fiber until callback done.
                      _     <- IO(scribe.info(s"Got The Returned Callbacl Login  Data: ${oprint(login)}"))
                      _     <- IO.sleep(5.seconds)                 // To delay closing down the HTTP server until OK response sent, that that is matters
                      _     <- IO(scribe.info(s"Finished, the HTTPServer will go away quickly}"))
                    } yield login
                }
                // I don't get why I am getting Received premature EOForg.http4s.InvalidBodyException when blaze server is shutting down
                // This is happening even when the browser is displaying the result. It must be during closing of keep-alive sockets.
                // log threads
                phase1Login.flatMap {
                  login =>
                    for {
                      _      <- IO(scribe.info(s"Got Manual Login  $login"))
                      access <- Authentication.getAccessToken(login.verifier, rqToken, login.authToken, login.config)
                      _       = IO(scribe.info(s"Got ACCESS Token $access"))
                      updated = login.copy(accessToken = access.some)
                    } yield updated
                }
            }
      }
  }

}
