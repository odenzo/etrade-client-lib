package com.odenzo.etrade.oauth

import cats.*
import cats.data.*
import cats.implicits.*
import cats.effect.*
import cats.effect.implicits.*
import com.github.blemale.scaffeine

import java.util.UUID

/** Decided to try scaffeine as a dedicated oauth session cache for independance. Actually isnt used by lib. */
type UUIDCache = scaffeine.Cache[UUID, OAuthSessionData]

object OAuthCache {
  import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
  import scala.concurrent.duration._

  def create: Resource[IO, scaffeine.Cache[UUID, OAuthSessionData]] = Resource.make(initializeCache)(release)

  /** Create a scaffeine cache a a resource */
  private def initializeCache: IO[scaffeine.Cache[UUID, OAuthSessionData]] = {
    IO {
      Scaffeine()
        .expireAfterAccess(30.minutes)
        .maximumSize(500)
        .build[UUID, OAuthSessionData]()
    }
  }

  private def release(r: scaffeine.Cache[UUID, OAuthSessionData]) = IO(r.cleanUp()).void
}
