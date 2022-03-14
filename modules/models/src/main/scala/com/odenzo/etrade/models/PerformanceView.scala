package com.odenzo.etrade.models

import io.circe.Codec

import java.time.Instant

case class PerformanceView(
    change: BigDecimal,               //	The change
    changePct: BigDecimal,            //	The change percentage
    lastTrade: BigDecimal,            //	The last trade
    daysGain: Option[BigDecimal],     //	The gain over the day
    totalGain: Option[BigDecimal],    //	The total gain
    totalGainPct: Option[BigDecimal], //	The total gain percentage
    marketValue: Option[BigDecimal],  //	The market value
    quoteStatus: QuoteStatus,         //	The quote type	REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
    lastTradeTime: ETimestamp         // (int64)	The time of the last trade
) derives Codec.AsObject
