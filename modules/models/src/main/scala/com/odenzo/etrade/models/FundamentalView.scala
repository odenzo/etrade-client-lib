package com.odenzo.etrade.models

import io.circe.Codec

import java.time.Instant

case class FundamentalView(
    astTrade: Option[BigDecimal], //	The last trade total
    change: BigDecimal,           //	The change
    changePct: BigDecimal,        //	The change percentage
    divYield: BigDecimal,         //	The dividend yield
    dividend: BigDecimal,         //	The dividend
    eps: BigDecimal,              //	The earnings per share
    lastTradeTime: ETimestamp,    // (int64)	The time of the last trade
    marketCap: BigDecimal,        //	The market cap
    peRatio: BigDecimal,          //	The Price to Earnings (P/E) ratio
    quoteStatus: String,          //	The quote type	REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
    week52Range: String           //	The 52 week range
) derives Codec.AsObject
