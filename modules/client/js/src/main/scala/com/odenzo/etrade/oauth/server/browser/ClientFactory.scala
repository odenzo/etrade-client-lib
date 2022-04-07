package com.odenzo.etrade.oauth.server.browser

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import org.http4s.*
import org.http4s.client.*
import org.http4s.dom.FetchClientBuilder

object ClientFactory {
  def baseClientR[F[_]: Async](): Any = {
    val client = FetchClientBuilder[IO].create
    client
  }

}
