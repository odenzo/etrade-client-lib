package com.odenzo.etrade.oauth.server

import scala.concurrent.duration.*
import cats.effect.{Async, IO, Resource}
import com.comcast.ip4s
import com.comcast.ip4s.{Host, Port}
import fs2.concurrent.SignallingRef
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.*
import org.http4s.server.*
import org.http4s.{HttpApp, HttpRoutes}

/** Helper for create base Web Server and Web Clients */
object WebFactory {
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
  ): IO[Either[Nothing, Unit]] = {
    scribe.info(s"Setting Up Killable Server -- AUTOKILL")
    for {
      h        <- IO.fromOption(Host.fromString(host))(Throwable(s"Illegal Host $host"))
      p        <- IO.fromOption(Port.fromInt(port))(Throwable(s"Illegal Port $port"))
      _         = scribe.info(s"Will Run Server on $h:$p")
      serverR   = contructServer(h, p, app)
      theHunter = killSwitch.waitUntil(b => b)
      _        <- IO(scribe.info(s"About to RACE the server useForever and the hunter "))
      race     <- IO.race(serverR.useForever, theHunter)
      _        <- IO(scribe.info(s"Done Racing "))
    } yield race
  }

  def contructServer(host: ip4s.Host, port: ip4s.Port, app: HttpApp[IO]): Resource[IO, Server] = {
    EmberServerBuilder.default[IO].withHost(host).withPort(port).withoutTLS.withHttpApp(app).build
  }

  def contructCancellableServer(host: ip4s.Host, port: ip4s.Port, app: HttpApp[IO]): Resource[IO, Server] = {
    EmberServerBuilder.default[IO].withHost(host).withPort(port).withoutTLS.withHttpApp(app).withShutdownTimeout(20.seconds).build
  }

  def baseClientR[F[_]: Async]: Resource[F, Client[F]] = {
    val default: EmberClientBuilder[F] = EmberClientBuilder.default[F]
    default.build
  }

}
