package com.odenzo.etrade.api

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import org.http4s.client.Client
import org.http4s.{Request, Uri}

/**
  * Context needed to create HTTPRequests to ETrade and ensure Client[IO] is available. This was an experiment in context functions for late
  * binding, not sure its worth it really, but OK.
  */
case class ETradeContext(apiUrl: Uri)

/**
  *   - Experiment with context functions (Scala 3), messes with Kleisi and things like that.
  *   - We build a Context that has information for constructing the URL. Adding more thing in here (at trait level) allows access to the
  *     context functions. So, the request builders can say baseUrl for instance. If adapting this would add your account name or some
  *     additional functions.
  */

type ETradeRequest[T] = ETradeContext ?=> T
type ETradeCall       = ETradeRequest[IO[Request[IO]]]

/**
  * This gives us the context function, and errors are raised in IO (mostly). Not it assumes that the client has middleware to do signing.
  * This marker is applied to Services which require the givens ETradeContext to create requests and the Client[IO] to action the request.
  */
type ETradeService[T] = (ETradeContext, Client[IO]) ?=> IO[T]

/**
  * baseUri contextual function, allows us to use in functions of type ETradeRequest "magically" using the ETradeContext available in the
  * caller's scope, not this scope.
  */
val baseUri: ETradeRequest[Uri] = summon[ETradeContext].apiUrl
val v1: ETradeRequest[Uri]      = summon[ETradeContext].apiUrl / "v1"
