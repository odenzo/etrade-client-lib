package com.odenzo.etrade.client.engine

import cats.effect.*
import com.odenzo.etrade.oauth.OAuthSessionData
import org.http4s.client.*
import org.http4s.*
import io.circe.Decoder

/**
  * Time for a perhaps worthless typeclass, I want to handle a Request[IO] and suspend an optional T to say how to decode the result on
  * success. There is no reason not to capture the T on the fetch method itelf though, with a Decoder
  */
