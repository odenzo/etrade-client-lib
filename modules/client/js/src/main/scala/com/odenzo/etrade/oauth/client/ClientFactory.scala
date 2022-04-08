package com.odenzo.etrade.oauth.client

import cats.effect.Resource
import cats.effect.Async
import org.http4s.client.Client
import org.http4s.dom.FetchClientBuilder
import org.scalajs.dom.{RequestCredentials, RequestMode};

/** SCalaJS Only Create base clients which can be wrapped in middle-ware */
object ClientFactory {

  private def default[F[_]: Async] = FetchClientBuilder[F]
    .withDefaultCache
    .withMode(RequestMode.`no-cors`)
    .withCredentials(RequestCredentials.`same-origin`)

  def baseClient[F[_]: Async](): Client[F]               = default[F].create
  def baseClientR[F[_]: Async](): Resource[F, Client[F]] = default[F].resource

}
