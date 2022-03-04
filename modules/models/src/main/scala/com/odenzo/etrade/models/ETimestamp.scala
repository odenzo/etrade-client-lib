package com.odenzo.etrade.models

import io.circe.*
import io.circe.Encoder.*
import io.circe.Decoder.*

import java.time.Instant

/**
  * int64 millies since epoch (think thi i secs no millisec but maybe forgetting. but decoding as Instant not happy,s TODO: REVISIT as
  * opaque type ot an anyval style
  */
case class ETimestamp(click: Instant) {
  def isDefined: Boolean = click != ETimestamp.ZERO
}

object ETimestamp:

  val ZERO: Instant = Instant.EPOCH

  // Coode Model an ETimestamp(c: Option[Instant]
  given codec: Codec[ETimestamp] = Codec.from(
    decodeLong.map(tick => ETimestamp(Instant.ofEpochMilli(tick))),
    encodeLong.contramap[ETimestamp]((ts: ETimestamp) => ts.click.toEpochMilli)
  )
