package com.odenzo.etrade.models

import io.circe.Codec
import java.time.Instant

case class Quick(
    lastTrade: BigDecimal,
    lastTradeTime: Instant,
    change: BigDecimal,
    changePct: BigDecimal,
    volume: BigDecimal,
    quoteStatus: String
) derives Codec.AsObject
