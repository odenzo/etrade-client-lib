package com.odenzo.etrade.models

import io.circe.Codec
import java.time.Instant

case class QuickView(
    lastTrade: BigDecimal,
    lastTradeTime: ETimestamp,
    change: BigDecimal,
    changePct: BigDecimal,
    volume: BigDecimal,
    quoteStatus: String,
    evenDayCurrentYield: Option[BigDecimal],    // Percentage
    annualTotalReturn: Option[BigDecimal],      // Percentage
    weightedAverageMaturity: Option[BigDecimal] // Percentage
) derives Codec.AsObject
