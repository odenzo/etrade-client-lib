package com.odenzo.etradeapp.purebackend

import cats.effect.IO
import io.circe.*
import io.circe.syntax.*
import org.http4s.client.oauth1.Token
import os.Path

case class CachedAccessTokens(request: Token, access: Token) derives Codec.AsObject

object CachedAccessTokens {
  val cacheFile: Path = os.home / "etrade_token_cache.json"

  /** This may fail if the file doesn't exist or is corrupted etc, in which case should assume no existing tokens and get new ones. */
  def read(): IO[CachedAccessTokens] =
    for {
      txt    <- IO(os.read(cacheFile))
      tokens <- IO.fromEither(io.circe.parser.decode[CachedAccessTokens](txt))
      _       = scribe.info(s"Read Cached Tokens: ${pprint(tokens)}")
    } yield tokens

  /** This should never fail */
  def write(tokens: CachedAccessTokens): IO[Unit] =
    for {
      _   <- IO(scribe.info(s"Saving Tokens ${pprint(tokens)} to $cacheFile"))
      res <- IO(os.write(cacheFile, tokens.asJson.spaces4))
    } yield ()

}
