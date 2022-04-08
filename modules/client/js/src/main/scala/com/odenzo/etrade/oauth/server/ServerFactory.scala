package com.odenzo.etrade.oauth.server

import cats.effect.{IO, Resource}
import com.comcast.ip4s
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.server.Server

object ServerFactory {
  def contructServer(host: ip4s.Host, port: ip4s.Port, routes: IO[HttpRoutes[IO]]): Resource[IO, Server] = {

    org.http4s.dom.ServiceWorker.addFetchEventListener(routes)
    // EmberServerBuilder.default[IO].withHost(host).withPort(port).withoutTLS.withHttpApp(app).build
  }
}
