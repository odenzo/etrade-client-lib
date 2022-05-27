package com.odenzo.etrade.models

import io.circe.*
import io.circe.Decoder.*
import io.circe.Encoder.*

import java.time.{Instant, ZoneId}

/** Some things, date so far, use millis as opposed to Epoch Seconds. */
case class EDateStamp(click: Instant) {
  def isDefined: Boolean = click != EDateStamp.ZERO
  // def toDate = click.atZone(ZoneId.) // EST New York time?
}

object EDateStamp:

  val ZERO: Instant = Instant.EPOCH

  given codec: Codec[EDateStamp] = Codec.from(
    decodeLong.map(tick => EDateStamp(Instant.ofEpochMilli(tick))), // .prepare(_.up), // AnyVal Hack
    encodeLong.contramap[EDateStamp]((ts: EDateStamp) => ts.click.toEpochMilli)
  )
