package com.odenzo.etrade.models

import io.circe.*
import io.circe.Decoder.*
import io.circe.Encoder.*

import java.time.{Instant, ZoneId}

/**
  * Some things, date so far, use millis as opposed to Epoch Seconds. Note that there are also some YYYYMMDD formatted dates. This doesn't
  * handle those (yet). Use LocalDate directly
  */
case class EDateStamp(click: Instant) {
  def isDefined: Boolean = click != EDateStamp.ZERO
  // def toDate = click.atZone(ZoneId.) // EST New York time?

}

object EDateStamp:

  val ZERO: Instant   = Instant.EPOCH
  val NOW: EDateStamp = EDateStamp(Instant.now())

  given codec: Codec[EDateStamp] = Codec.from(
    decodeLong.map(tick => EDateStamp(Instant.ofEpochMilli(tick))), // .prepare(_.up), // AnyVal Hack
    encodeLong.contramap[EDateStamp]((ts: EDateStamp) => ts.click.toEpochMilli)
  )
