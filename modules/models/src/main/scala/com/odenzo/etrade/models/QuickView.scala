package com.odenzo.etrade.models

import io.circe.Codec
import java.time.Instant

case class QuickView(
    lastTrade: BigDecimal,
    lastTradeTime: Instant,
    change: BigDecimal,
    changePct: BigDecimal,
    volume: BigDecimal,
    quoteStatus: String,
    evenDayCurrentYield: BigDecimal,    // Percentage
    annualTotalReturn: BigDecimal,      // Percentage
    weightedAverageMaturity: BigDecimal // Percentage
) derives Codec.AsObject
