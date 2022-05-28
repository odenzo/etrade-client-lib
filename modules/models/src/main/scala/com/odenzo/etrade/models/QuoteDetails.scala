package com.odenzo.etrade.models

import cats.data.Chain
import com.odenzo.etrade.base.CirceUtils

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.Instant

sealed trait QuoteDetails

// Can't decide if I should find unique fields to "select" which object we have, or use the field from the enclosing class (QuoteRs)
// Better to to on level above I think
//val selector = Map("availability" -> summon[Decoder[MutualFund]])

/** max inlines problem not here. As */
case class MutualFundQuoteDetails(
    actual12B1Fee: Option[BigDecimal],                      //	The annual marketing or distribution fee on the mutual fund
    annualTotalReturn: BigDecimal,                          //	number (double)	The annual total return
    availability: String,
    averageAnnualReturn10Yr: BigDecimal,                    //	number (double)	The average annual return for ten years
    averageAnnualReturn1Yr: BigDecimal,                     //	number (double)	The average annual return for one year
    averageAnnualReturn3Yr: BigDecimal,                     //	number (double)	The average annual return for three years
    averageAnnualReturn5Yr: BigDecimal,                     //	number (double)	The average annual return for five years
    averageAnnualReturns: Option[BigDecimal],               //	number (double)	The average annual return at the end of the quarter; this is available if fund// has been active for more than 10 years
    changeClose: BigDecimal,
    changeClosePercentage: Double,
    cusip: String,
    deferredSalesCharges: Option[Chain[SalesChargeValues]], // 	The deferred sales charge
    earlyRedemptionFee: Option[String],
    etradeEarlyRedemptionFee: Option[String],               //	string	The E*TRADE early redemption fee
    exchangeCode: String,                                   //	string	The code of the exchange
    frontEndSalesCharges: Option[Chain[SalesChargeValues]], //	array[SaleChargeValues]	The front-end sales charge
    fundFamily: String,
    fundInceptionDate: EDateStamp,                          //	integer (int64)	The date when the fund started
    fundName: String,
    grossExpenseRatio: BigDecimal,
    high52: BigDecimal,                                     //	number (double)	The highest price at which a security has traded during the past year
    initialInvestment: BigDecimal,
    initialIraInvestment: BigDecimal,
    lastTrade: BigDecimal,                                  //	number (double)	The price of the most recent trade of the security
    low52: BigDecimal,                                      // 	number (double)	The lowest price at which a security has traded during the past year
    maxSalesLoad: Option[BigDecimal],                       //	number	The maximum sales charge
    monthlyTrailingReturn10Y: Option[BigDecimal],           // 	number	The ten-year monthly trailing return value
    monthlyTrailingReturn1M: Option[BigDecimal],            // 	The one-month monthly trailing return value
    monthlyTrailingReturn1Y: Option[BigDecimal],            //	number	The one-year monthly trailing return value
    monthlyTrailingReturn3M: Option[BigDecimal],            // 	The three-month monthly trailing return value
    monthlyTrailingReturn3Y: Option[BigDecimal],            // 	number	The three-year monthly trailing return value
    monthlyTrailingReturn5Y: Option[BigDecimal],            // 	number	The five-year monthly trailing return value
    monthlyTrailingReturn6M: Option[BigDecimal],            // 	The six-month monthly trailing return value
    monthlyTrailingReturnYTD: Option[BigDecimal],           // 	number	The year-to-date monthly trailing return value
    morningStarCategory: Option[String],                    //	string	The Morningstar category for the fund
    netAssetValue: BigDecimal,
    netAssets: NetAssets,
    netExpenseRatio: BigDecimal,
    orderCutoffTime: Int,                                   // HHmmm, e.g. 1600 = 4PM each day
    performanceAsOfDate: Option[String],                    //	string	The start date the performance is measured from
    previousClose: BigDecimal,
    publicOfferPrice: BigDecimal,
    qtrlyPerformanceAsOfDate: Option[String],               // 	string	The start date of the quarter that the performance is
    // measured from
    qtrlyTrailingReturn1M: Option[BigDecimal],              // 	The one-month quarterly trailing return value
    qtrlyTrailingReturn3M: Option[BigDecimal],              // 	The three-month quarterly trailing return value
    qtrlyTrailingReturn6M: Option[BigDecimal],              // 	The six-month quarterly trailing return value
    qtrlyTrailingReturnYTD: Option[BigDecimal],             // 	The year-to-date quarterly trailing return value
    quarterlySinceInception: Option[BigDecimal],            //	number	The quarterly average value of the fund since the beginning of fund
    redemption: Option[Redemption],                         // 	The mutual fund shares redemption properties
    salesCharge: String,
    sevenDayCurrentYield: BigDecimal,                       //	number (double)	The seven-day current yield
    sinceInception: BigDecimal,                             //	number	The value of the fund since its beginning
    subsequentInvestment: BigDecimal,
    subsequentIraInvestment: BigDecimal,
    symbolDescription: String,
    timeOfLastTrade: ETimestamp,
    transactionFee: String,                                 // yes/no
    week52HiDate: ETimestamp,                               //	integer (int64)	The date when the price was the highest in the last 52 weeks
    week52LowDate: ETimestamp,                              //	integer (int64)	The date when the price was the lowest in the last 52 weeks
    weightedAverageMaturity: BigDecimal                     //	number (double)	The weighted average of maturity
) extends QuoteDetails

object MutualFundQuoteDetails:
  import com.odenzo.etrade.models.codecs.given
  val rename                                          = Map("netAssets" -> "NetAssets")
  given codec: Codec.AsObject[MutualFundQuoteDetails] = CirceUtils.renamingCodec(deriveCodec[MutualFundQuoteDetails], rename)

/**
  * Before or After Hourss, this is embedded not top level?
  *
  * @param lastPrice
  * @param change
  * @param percentChange
  * @param bid
  * @param bidSize
  * @param ask
  * @param askSize
  * @param volume
  * @param timeOfLastTrade
  * @param timeZone
  * @param quoteStatus
  */
case class ExtendedHourQuoteDetail(
    ask: BigDecimal,           //  (double)	The ask price of the symbol
    askSize: Long,             // integer (int64)	The number of shares or contracts offered by a broker or dealer at the ask price
    bid: BigDecimal,           // 	number (double)	The bid price of the symbol
    bidSize: Long,             //	integer (int64)	The number of shares or contracts offered by a broker or dealer at the bid price
    change: BigDecimal,        // 	number (double)	The dollar value of the difference between the previous and the present executed price
    lastPrice: BigDecimal,     // 	number (double)	The price of the most recent trade of a security
    percentChange: BigDecimal, // 	number (double)	The percentage value of difference between the previous and the present executed price
    quoteStatus: String,       // 	string	The status of the quote, either delayed or real time	REALTIME, DELAYED, CLOSING, EH_REALTIME,
    timeOfLastTrade: Instant,  // 	integer (int64)	The time when the last trade was carried out for the symbol
    timeZone: Option[String],  //	string	The time zone corresponding to the timestamp provided in the quote response
    volume: Long               //	integer (int64)	The number of shares or contracts
    // EH_BEFORE_OPEN, EH_CLOSED
) extends QuoteDetails derives Codec.AsObject

/*
  "Intraday" : {
                    "ask" : 94.0,
                    "bid" : 93.86,
                    "changeClose" : -6.98,
                    "changeClosePercentage" : -6.92,
                    "companyName" : "CLOUDFLARE INC CL A COM",
                    "high" : 105.34,
                    "lastTrade" : 93.93,
                    "low" : 92.14,
                    "totalVolume" : 4765913
                },
 */

/** If Fundamental key is in base response. This seems complete */
case class FundamentalQuoteDetails(
    companyName: String,      //	string	The name of the company associated with the equity, option, or index.
    eps: BigDecimal,          //	number (double)	The earnings per share on a rolling basis (Applies to stocks only)
    estEarnings: BigDecimal,  //	number (double)	The estimated earnings
    high52: BigDecimal,       //	number (double)	The highest price at which a security has traded during the past year (52 weeks). For options, this value is the lifetime high.
    lastTrade: BigDecimal,    //	number (double)	The most recent trade price for a security
    low52: BigDecimal,        //	number (double)	The lowest price at which a security has traded during the past year (52 weeks). For options, this value is the lifetime low.
    nextEarningDate: Option[String],
    symbolDescription: String //	string	A description of the security, such as company name or option description
) extends QuoteDetails derives Codec.AsObject

/*
  "Fundamental" : {
                    "companyName" : "CLOUDFLARE INC CL A COM",
                    "eps" : -0.8289,
                    "estEarnings" : 0.031,
                    "high52" : 221.64,
                    "lastTrade" : 94.145,
                    "low52" : 61.77,
                    "symbolDescription" : "CLOUDFLARE INC CL A COM"
                },
 */

// This is the answer I get back after hours at leasts (for a STK)
case class IntraDayQuoteDetails(
    adjustedFlag: Option[Boolean],
    ask: BigDecimal,                   // 	number (double)	The current ask price for a security
    //  askSize: Long,
    //   askTime: Option[String],           // Weird dateimte at EST Seconds
    bid: BigDecimal,                   //  (double)	The current bid price for a security
    //   bidExchange: Option[String],
    //  bidSize: Long,
    //   bidTime: Option[String],
    changeClose: BigDecimal,           //  (double)	The dollar change of the last price from the previous close
    changeClosePercentage: BigDecimal, //  (double)	The percentage change of the last price from the previous close (e.g. -0.16 , is .16%)
    companyName: String,               //	string	The name of the company associated with the equity, option, or index
    //   daysToExpiration: Option[Long],
//    dirLast: String,
    //   dividend: BigDecimal,
    //   eps: BigDecimal,
//    estEarnings: BigDecimal,
//    exDividendDate: ETimestamp,
    high: BigDecimal,                  //  (double)	The highest price at which a security has traded during the current day
//    high52: BigDecimal,                //  (double)	The highest price at which a security has traded during the current day
    lastTrade: BigDecimal,             //  (double)	The price of the last trade
    low: BigDecimal,                   //  (double)	The lowest price at which a security has traded during the current day
//    low52: BigDecimal,                 //  (double)	The lowest price at which a security has traded during the current day
//    open: BigDecimal,
//    openInterest: Long,
//    optionStyle: Option[String],
//    optionUnderlier: Option[String],
//    previousClose: BigDecimal,
//    previousDayVolume: Long,
//    upc: Option[Long],
//    cashDeliverable: Option[BigDecimal],
    totalVolume: Long                  //	integer (int64)	Total number of shares or contracts exchanging hands
    // Price adjusted for CA or Dividends
) extends QuoteDetails derives Codec.AsObject

case class AllQuoteDetails(
    adjustedFlag: Option[Boolean],
    ask: BigDecimal,                   // 	number (double)	The current ask price for a security
    askSize: Long,
    askTime: Option[String],           // Weird dateimte at EST Seconds
    bid: BigDecimal,                   //  (double)	The current bid price for a security
    bidExchange: Option[String],
    bidSize: Long,
    bidTime: Option[String],
    changeClose: BigDecimal,           //  (double)	The dollar change of the last price from the previous close
    changeClosePercentage: BigDecimal, //  (double)	The percentage change of the last price from the previous close
    companyName: String,               //	string	The name of the company associated with the equity, option, or index
    daysToExpiration: Option[Long],
    dirLast: String,
    dividend: BigDecimal,
    eps: BigDecimal,
    estEarnings: BigDecimal,
    exDividendDate: ETimestamp,
    high: BigDecimal,                  //  (double)	The highest price at which a security has traded during the current day
    high52: BigDecimal,                //  (double)	The highest price at which a security has traded during the current day
    lastTrade: BigDecimal,             //  (double)	The price of the last trade
    low: BigDecimal,                   //  (double)	The lowest price at which a security has traded during the current day
    low52: BigDecimal,                 //  (double)	The lowest price at which a security has traded during the current day
    open: BigDecimal,
    openInterest: Long,
    optionStyle: Option[String],
    optionUnderlier: Option[String],
    previousClose: BigDecimal,
    previousDayVolume: Long,
    upc: Option[Long],
    cashDeliverable: Option[BigDecimal],
    totalVolume: Long,                 //	integer (int64)	Total number of shares or contracts exchanging hands
    // Price adjusted for CA or Dividends
    extendedHourQuoteDetail: Option[ExtendedHourQuoteDetail]
) extends QuoteDetails derives Codec.AsObject

/*
         "adjustedFlag": false,
        IQD  "ask": 100.88,
          "askSize": 100,
          "askTime": "20:04:01 EST 03-03-2022",
        IQD  "bid": 95.73,
          "bidExchange": " ",
          "bidSize": 100,
          "bidTime": "20:04:01 EST 03-03-2022",
       IQD   "changeClose": -3.09,
       IQD   "changeClosePercentage": -2.97,
   FQD    IQD   "companyName": "CLOUDFLARE INC CL A COM",

          "daysToExpiration": 0,
          "dirLast": "1",
          "dividend": 0.0,
          "eps": -0.8289,
      FQD    "estEarnings": 0.031,
          "exDividendDate": 0,
       IQD   "high": 109.13,
          "high52": 221.64,
          "lastTrade": 100.91,
          "low": 97.27,
          "low52": 61.77,
          "open": 106.87,
          "openInterest": 0,
          "optionStyle": "",
          "optionUnderlier": "",
          "previousClose": 104.0,
          "previousDayVolume": 4890092,
          "primaryExchange": "NYSE",
          "symbolDescription": "CLOUDFLARE INC CL A COM",
          "totalVolume": 5120956,
          "upc": 0,
          "cashDeliverable": 0,
          "marketCap": 27637533530.00,
          "sharesOutstanding": 273883000,
          "nextEarningDate": "02/10/2022",
          "beta": 2.5,
          "yield": 0.0,
          "declaredDividend": 0.0,
          "dividendPayableDate": 0,
          "pe": 0.0,
          "week52LowDate": 1615186088,
          "week52HiDate": 1637218088,
          "intrinsicValue": 0.0,
          "timePremium": 0.0,
          "optionMultiplier": 0.0,
          "contractSize": 0.0,
          "expirationDate": 0,
          "timeOfLastTrade": 1646428200,
          "averageVolume": 6468285,
          "ExtendedHourQuoteDetail": {
            "lastPrice": 100.91,
            "change": -3.09,
            "percentChange": -2.97,
            "bid": 95.73,
            "bidSize": 100,
            "ask": 100.88,
            "askSize": 100,
            "volume": 5120956,
            "timeOfLastTrade": 1646355841,
            "timeZone": "",
            "quoteStatus": "EH_BEFORE_OPEN"
          }
 */
case class OptionQuoteDetails(
    // "Option" : {
    //  "ask" : 102.0,
    //  "askSize" : 500,
    //  "bid" : 100.25,
    //  "bidSize" : 100,
    //  "companyName" : "CLOUDFLARE INC CL A COM",
    //  "daysToExpiration" : 0,
    //  "lastTrade" : 101.21,
    //  "openInterest" : 0,
    //  "optionPreviousBidPrice" : 0,
    //  "optionPreviousAskPrice" : 0,
    //  "osiKey" : "",
    //  "intrinsicValue" : 0.0,
    //  "timePremium" : 0.0,
    //  "optionMultiplier" : 0.0,
    //  "contractSize" : 0.0,
    //  "symbolDescription" : "CLOUDFLARE INC CL A COM"
    // },

) extends QuoteDetails derives Codec.AsObject

/** Looks OK for stocks, low52 is zero though */
case class Week52(
    companyName: String,
    high52: BigDecimal,        //  (double)	The highest price at which a security has traded during the current day
    lastTrade: BigDecimal,     //  (double)	The price of the last trade
    low52: BigDecimal,
    perf12Months: BigDecimal,
    previousClose: BigDecimal,
    symbolDescription: String, //	string	The name of the company associated with the equity, option, or index
    totalVolume: Long
) extends QuoteDetails derives Codec.AsObject
