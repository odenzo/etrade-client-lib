package com.odenzo.etrade
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.oauth.config.OAuthConfig
import os.*
import io.circe.*
import io.circe.syntax.*
import cats.effect.*
import cats.effect.syntax.all.*
import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.client.engine.ETradeContext
import com.odenzo.etrade.client.services.Services
import com.odenzo.etrade.models.responses.ListAccountsRs
import com.odenzo.etrade.oauth.{Authentication, OAuthSessionData}
import com.odenzo.etrade.oauth.client.OAuthClient
import org.http4s.client.Client
import org.http4s.client.oauth1.Token

import java.util.UUID
import scala.util.{Failure, Success, Try}

/** Offline Token Cache writing to disk in place of Redis/DB etc for development. Inside the cache is a simple JSON format */
object TokenCache {
  import io.circe.generic.auto.*

  case class CachedTokens(requestToken: Token, accessToken: Token, isSandbox: Boolean) derives Codec.AsObject

  val cacheFile: os.Path = home / ".etrade-cache"

  /**
    * Tries to use cached token to get a new access token. Throws if fails (i.e. no cached tokens, refresh token fails), in which case a
    * full login is needed.
    */
  def refreshCachedTokens(config: OAuthConfig, sandbox: Boolean)(using ctx: ETradeContext): IO[OAuthSessionData] =
    // Get the cached auth and access token, see if the access token still works.
    // If doens't see if we can get a new access token from the auth token
    // We could see if the access token still works, but a quick hack to get new one always.

    for {
      cached       <- fetchCachedTokens
      _            <- IO.whenA(cached.isSandbox != sandbox)(deleteCache())
      _            <- IO.raiseWhen(cached.isSandbox != sandbox)(Throwable("HAve to login manually when switching from Sandbox"))
      cachedSession = rebuildSessionDate(cached.requestToken, cached.accessToken, config)
      _             = scribe.info(s"Cached Session: ${oprint(cached)} -> ${oprint(cachedSession)} ")
      newSession   <- sampleServiceCall(cachedSession).handleErrorWith { err =>
                        scribe.error(s"Just a warning, calling health fn returned error with cached token/refreshing $err")
                        refreshToken(cachedSession)
                      }
      _            <- storeNewLogin(newSession, sandbox)
    } yield newSession

  end refreshCachedTokens

  def rebuildSessionDate(rq: Token, access: Token, config: OAuthConfig): OAuthSessionData = OAuthSessionData(
    accessToken = access,
    rqToken = rq,
    config = config
  )

  /** The simplest etrade call to see if access token is still valid. */
  def sampleServiceCall(sessionData: OAuthSessionData)(using context: ETradeContext): IO[OAuthSessionData] = OAuthClient
    .signingDebugClient(sessionData)
    .use { client =>
      given Client[IO] = client
      scribe.info("Checking listAccounts")
      Services.listAccountsApp() *> IO.pure(sessionData)
    }

  def refreshToken(sessionData: OAuthSessionData)(using context: ETradeContext): IO[OAuthSessionData] = {
    OAuthClient
      .debugClient
      .use { c =>
        scribe.warn("Acces Token Health Check Failed: Trying to Refresh Access Token")
        given Client[IO] = c
        Authentication.refreshAccessToken(sessionData.config, sessionData.rqToken).map(access => sessionData.setAccessToken(access))
      }
  }

  def fetchCachedTokens: IO[CachedTokens] = IO {
    val txt = os.read(cacheFile)
    io.circe.parser.decode[CachedTokens](txt).fold(e => throw e, v => v)
  }

  def storeCachedTokens(ct: CachedTokens): IO[Unit] = IO {
    os.write.over(cacheFile, ct.asJson.spaces4)
  }

  def storeNewLogin(newLogin: OAuthSessionData, sandbox: Boolean): IO[Unit] =
    val ct = CachedTokens(newLogin.rqToken, newLogin.accessToken, sandbox)
    storeCachedTokens(ct)

  def deleteCache(): IO[Unit] = IO(os.remove(cacheFile)) *> IO(scribe.warn("Deleted Cache"))
}
