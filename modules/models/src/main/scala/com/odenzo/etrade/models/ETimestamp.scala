package com.odenzo.etrade.models

import io.circe.*
import io.circe.Encoder.*
import io.circe.Decoder.*

import java.time.{Instant, ZoneId}

/** int64 secoond since epoch (confirmed) TODO: opaque type ot an anyval style */
case class ETimestamp(click: Instant) {
  def isDefined: Boolean = this != ETimestamp.ZERO
}

object ETimestamp:

  val ZERO: ETimestamp = ETimestamp(Instant.EPOCH)
  def NOW: ETimestamp  = ETimestamp(Instant.now())

  given codec: Codec[ETimestamp] = Codec.from(
    decodeLong.map(tick => ETimestamp(Instant.ofEpochSecond(tick))), // .prepare(_.up), // AnyVal Hack
    encodeLong.contramap[ETimestamp]((ts: ETimestamp) => ts.click.getEpochSecond)
  )
