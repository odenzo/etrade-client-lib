package com.odenzo.etrade.models

/** int64 millies since epoch, but decoding as Instant not happy,s */
case class ETTimestamp(click: Long)

object ETTimestamp {
//  implicit val decoder = Decoder[Instant].
}
