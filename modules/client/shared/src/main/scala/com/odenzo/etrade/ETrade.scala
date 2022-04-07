package com.odenzo.etrade

import cats.effect.{IO, Resource}
import com.odenzo.etrade.api.models.*
import com.odenzo.etrade.oauth.OAuthLogic
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.oauth1.Token

/** This interface is just a facade for library clients to configure easily. */
object ETrade {

  def something = "notyet"
}
