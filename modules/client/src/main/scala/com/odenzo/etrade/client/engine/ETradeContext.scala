package com.odenzo.etrade.client.engine

import cats.conversions.all.autoWidenFunctor
import cats.effect.IO
import cats.implicits.catsSyntaxOptionId
import com.odenzo.etrade.oauth.{OAuthConfig, OAuthSessionData}
import org.http4s.Uri.*
import org.http4s.client.Client
import org.http4s.client.oauth1.{Consumer, Token}
import org.http4s.implicits.uri
import org.http4s.{Request, Uri}

import java.time.Instant
import java.util.UUID

/**
  * This is ETrade specific I guess, but a general approach for any sight. We build a Context that has information for constructing the URL.
  * Adding more thing in here (at trait level) allows access to the context functions. So, the request builders can say baseUrl for
  * instance. If adapting this would add your account name or some additional functions.
  */

type ETradeRequest[T] = ETradeContext ?=> T
type ETradeCall       = ETradeRequest[IO[Request[IO]]]

/**
  * This gives us the context function, and errors are raised in IO (mostly). Not it assumes that the client has middleware to do signing.
  * One is upplied in oauth.clients package.
  */
type ETradeService[T] = (ETradeContext, Client[IO]) ?=> IO[T]

/**
  * State that tracks the OAuth Session, further scoped down to a single session per run rather than dealing with multiple logged in users
  * anymore. (So essentially its almost a singleton, but we may refresh this so its actually mutable session state) We will use scalacache
  * to deal with it, since thats what the previous full system used for sessions. Should parameterize key and make a trait too
  */
case class ETradeContext(apiUrl: Uri)

/** baseUri contextual function, allows us to use in functions of type ETradeRequest "magically" */
val baseUri: ETradeRequest[Uri] = summon[ETradeContext].apiUrl
val v1: ETradeRequest[Uri]      = summon[ETradeContext].apiUrl / "v1"
