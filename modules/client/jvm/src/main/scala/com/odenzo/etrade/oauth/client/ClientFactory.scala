package com.odenzo.etrade.oauth.client

import cats.effect.*
import cats.effect.syntax.all.*

import cats.*
import cats.data.*
import cats.syntax.all.*

import com.odenzo.etrade.api.models.ETradeConfig
import org.http4s.*
import org.http4s.ember.client.*
import org.http4s.client.*
object ClientFactory {
  def baseClientR[F[_]: Async](): Resource[F, Client[F]] = {
    val default: EmberClientBuilder[F] = EmberClientBuilder.default[F]
    default.build
  }

}
