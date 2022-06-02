package com.odenzo.etradeapp.purebackend

import cats.effect.IO
import io.circe.*
import io.circe.syntax.*
import org.http4s.client.oauth1.Token
import os.Path

case class CachedAccessTokens(request: Token, access: Token) derives Codec.AsObject

object CachedAccessTokens {

  /** Be Very Carefule with this, essentially clearCache is rm -rf on this. */
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

  def clearCache(): IO[Unit] =
    IO.raiseWhen(cacheFile.ext != "json" || !os.isFile(cacheFile))(Throwable(s"Cache File should be *.json and a file, not $cacheFile"))
      *> IO.delay { if os.exists(cacheFile) then os.remove(cacheFile) }

}
