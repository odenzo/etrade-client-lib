package com.odenzo.etrade.oauth.client

import cats.effect.*
import org.http4s.*
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.*

object OAuthClient {

  /** Simple HTTP4S Client with no middleware */
  val clientR: Resource[IO, Client[IO]] = BlazeClientBuilder[IO].resource

  val simpleClient: Resource[IO, Client[IO]] = BlazeClientBuilder[IO].resource
}
