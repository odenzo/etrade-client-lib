package com.odenzo.etrade.models

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.Instant

case class Quick(
    lastTrade: BigDecimal,
    lastTradeTime: Instant,
    change: BigDecimal,
    changePct: BigDecimal,
    volume: BigDecimal,
    quoteStatus: String
)

object Quick {
  implicit val codec: Codec.AsObject[Quick] = deriveCodec[Quick]
}
