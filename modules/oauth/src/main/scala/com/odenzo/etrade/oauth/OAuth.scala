package com.odenzo.etrade.oauth

import cats.effect.{IO, Resource}
import com.github.blemale.scaffeine
import com.odenzo.etrade.oauth.config.OAuthConfig
import com.odenzo.etrade.oauth.server.OAuthServer
import org.http4s.Uri
import org.http4s.server.Server
import org.http4s.Uri.*

import java.util.UUID
import org.http4s.syntax.literals.uri

/** Main class to instanciate the system for a login, or multiple logins to a partocular host with app consumer keys */
class OAuth(val config: OAuthConfig) {

  val cacheR: Resource[IO, scaffeine.Cache[UUID, OAuthSessionData]] = OAuthCache.create

  def serverR(workerFn: IO[Unit], cache: scaffeine.Cache[UUID, OAuthSessionData]): Resource[IO, Server] =
    val defaultHost: Host = uri"http://localhost/".host.get
    OAuthServer.createServer(
      host = config.callbackUrl.host.getOrElse(defaultHost).value, // FIXME
      port = config.callbackUrl.port.getOrElse(5555),
      config = config,
      cache = cache,
      workerFn = workerFn
    )
  // Example Client Main See TestMain
}
