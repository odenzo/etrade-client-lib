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

case class CompleteView(
    priceAdjustedFlag: Boolean,         //	boolean	The price adjusted flag
    price: BigDecimal,                  //	The current market price
    adjPrice: BigDecimal,               //	The adjusted price
    change: BigDecimal,                 //	The change
    changePct: BigDecimal,              //	The change percentage
    prevClose: BigDecimal,              //	The previous close
    adjPrevClose: BigDecimal,           //	The adjusted previous close
    volume: BigDecimal,                 //	The volume
    lastTrade: BigDecimal,              //	The last trade
    lastTradeTime: Long,                // (int64)	The time of the last trade
    adjLastTrade: BigDecimal,           //	The adjusted last trade
    symbolDescription: String,          //	The symbol description
    perform1Month: Option[BigDecimal],  //	The one-month performance
    perform3Month: Option[BigDecimal],  //	The three-month performance
    perform6Month: Option[BigDecimal],  //	The six-month performance
    perform12Month: Option[BigDecimal], //	The 12-month performance
    prevDayVolume: Option[Long],        // (int64)	The previous day's volume
    tenDayVolume: Option[Long],         // (int64)	The 10 day average volume
    beta: BigDecimal,                   //	The beta
    sv10DaysAvg: BigDecimal,            //	The 10 day average stochastic volatility
    sv20DaysAvg: BigDecimal,            //	The 20 day average stochastic volatility
    sv1MonAvg: BigDecimal,              //	The one month average stochastic volatility
    sv2MonAvg: BigDecimal,              //	The two month average stochastic volatility
    sv3MonAvg: BigDecimal,              //	The three month average stochastic volatility
    sv4MonAvg: BigDecimal,              //	The four month average stochastic volatility
    sv6MonAvg: BigDecimal,              //	The six month average stochastic volatility
    week52High: BigDecimal,             //	The 52 week high
    week52Low: BigDecimal,              //	The 52 week low
    week52Range: String,                //	The 52 week range
    marketCap: BigDecimal,              //	The market cap
    daysRange: Option[String],          //	The day's range (Not for MF)
    delta52WkHigh: BigDecimal,          //	The high for the 52 week high/low delta calculation
    delta52WkLow: BigDecimal,           //	The low for the 52 week high/low delta calculation
    currency: String,                   //	The currency
    exchange: String,                   //	The exchange
    marginable: Boolean,                //	The sum available for margin
    bid: BigDecimal,                    //	The bid
    ask: BigDecimal,                    //	The ask
    bidAskSpread: BigDecimal,           //	The bid ask spread
    bidSize: Long,                      // (int64)	The size of the bid
    askSize: Long,                      // (int64)	The size of the ask
    open: BigDecimal,                   //	The open
    delta: BigDecimal,                  //	The delta
    gamma: BigDecimal,                  //	The gamma
    ivPct: BigDecimal,                  //	The Implied Volatility (IV) percentage
    rho: BigDecimal,                    //	The rho
    theta: BigDecimal,                  //	The theta
    vega: BigDecimal,                   //	The vega
    premium: BigDecimal,                //	The premium
    daysToExpiration: Option[Long],     // (int32)	The days remaining until expiration
    intrinsicValue: BigDecimal,         //	The intrinsic value
    openInterest: BigDecimal,           //	The open interest
    optionsAdjustedFlag: Boolean,       //	The options adjusted flag
    deliverablesStr: String,            //	The deliverables
    optionMultiplier: BigDecimal,       //	The option multiplier
    baseSymbolAndPrice: String,         //	The price of the underlying or base symbol
    estEarnings: BigDecimal,            //	The estimated earnings
    eps: BigDecimal,                    //	The earnings per share
    peRatio: BigDecimal,                //	The Price to Earnings (P/E) ratio
    annualDividend: BigDecimal,         //	The annual dividend
    dividend: BigDecimal,               //	The dividend
    divYield: BigDecimal,               //	The dividend yield
    divPayDate: Option[Long],           // (int64)	The date of the dividend pay // TODO: EDateStamp
    exDividendDate: Option[Long],       // (int64)	The extended dividend date TODO: EDateStamp
    cusip: String,                      //	The CUSIP :BigDecimal , //
    quoteStatus: QuoteStatus
) derives Codec.AsObject

case class OptionWatchView(
    baseSymbolAndPrice: String, //	The price of the underlying or base symbol of the option
    premium: BigDecimal,        //	The option premium
    lastTrade: BigDecimal,      //	The last trade
    bid: BigDecimal,            //	The bid
    ask: BigDecimal,            //	The ask
    quoteStatus: String,        //	The quote type	REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED
    lastTradeTime: Long         // (int64)	The time of the last trade
) derives Codec.AsObject
