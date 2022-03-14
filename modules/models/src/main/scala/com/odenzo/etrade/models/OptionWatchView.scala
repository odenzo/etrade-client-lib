package com.odenzo.etrade.models

import io.circe.Codec

import java.time.Instant

case class OptionWatchView(
    baseSymbolAndPrice: String, //	The price of the underlying or base symbol of the option
    premium: BigDecimal,        //	The option premium
    lastTrade: BigDecimal,      //	The last trade
    bid: BigDecimal,            //	The bid
    ask: BigDecimal,            //	The ask
    quoteStatus: String,        //	The quote type	REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
    lastTradeTime: Long         // (int64)	The time of the last trade
) derives Codec.AsObject
