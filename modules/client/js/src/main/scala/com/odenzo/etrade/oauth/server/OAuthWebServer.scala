package com.odenzo.etrade.oauth.server

import cats.effect.*
import cats.effect.implicits.*
import cats.effect.unsafe.*
import cats.effect.unsafe.IORuntime.global
import cats.implicits.*
import com.comcast.ip4s
import com.comcast.ip4s.*
import com.odenzo.etrade.api.models.{OAuthConfig, OAuthSessionData}
import com.odenzo.etrade.base.OPrint.oprint
import com.odenzo.etrade.oauth.*
import fs2.concurrent.SignallingRef
import org.http4s.*
import org.http4s.Uri.RegName
import org.http4s.client.Client
import org.http4s.client.oauth1.Token
import org.http4s.dsl.io.*
import org.http4s.server.{Router, Server}

import java.time.Instant
import java.util.UUID
import scala.concurrent.duration.*

/** This has one job, when the user logs into the web browser then browser invokes the callback and we extract login information. */
object OAuthWebServer {

  import cats.effect.unsafe.implicits.global

  /**
    * Okay, a little more separation of concerns but not complete. We make an HttpApp with routes. We then construct a pretty generic server
    * listening on given host and port. This is "run forever" and raced with killSwitch.waitUntil(_ === true) First one wins and cancels the
    * other one. So, flipping the kill switch stops the HTTP Server via cancellation. Blaze trick is different. Actually, any IO would be
    * OK. Even a IO.sleep(1000) to race.
    */
  def killableServer(
      host: String,
      port: Int,
      app: HttpApp[IO],
      killSwitch: SignallingRef[IO, Boolean]
  ): IO[Unit] = {

    val localhost: Hostname = host"localhost"
    val port5555: Port      = port"5555"

    val h: Host                       = Host.fromString(host).getOrElse(localhost)
    val p: Port                       = Port.fromInt(port).getOrElse(port5555)
    val serverR: Resource[IO, Server] = ServerFactory.contructServer(h, p, app)

    val theHunter: IO[Unit] = killSwitch.waitUntil(b => b === true)

    IO.race(serverR.useForever, theHunter).void
  }

}
