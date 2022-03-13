package com.odenzo.etrade.models.responses

import com.odenzo.base.OPrint.oprint
import io.circe.Decoder.Result
import io.circe.generic.semiauto.deriveDecoder
import io.circe.{Decoder, Json, JsonObject, ParsingFailure}
import munit.FunSuite

import javax.tools.ForwardingFileObject

class QuoteRsTest extends FunSuite {

  val jsonTxt: String =
    """  {
      |  "QuoteResponse" : {
      |    "QuoteData" : [
      |      {
      |        "dateTime" : "19:57:32 EST 03-07-2022",
      |        "dateTimeUTC" : 1646701052,
      |        "quoteStatus" : "CLOSING",
      |        "ahFlag" : "true",
      |        "hasMiniOptions" : false,
      |        "All" : {
      |          "adjustedFlag" : false,
      |          "ask" : 92.45,
      |          "askSize" : 100,
      |          "askTime" : "19:57:32 EST 03-07-2022",
      |          "bid" : 91.5,
      |          "bidExchange" : " ",
      |          "bidSize" : 500,
      |          "bidTime" : "19:57:32 EST 03-07-2022",
      |          "changeClose" : -0.01,
      |          "changeClosePercentage" : -0.01,
      |          "companyName" : "CLOUDFLARE INC CL A COM",
      |          "daysToExpiration" : 0,
      |          "dirLast" : "1",
      |          "dividend" : 0.0,
      |          "eps" : -0.8289,
      |          "estEarnings" : 0.031,
      |          "exDividendDate" : 0,
      |          "high" : 96.11,
      |          "high52" : 221.64,
      |          "lastTrade" : 92.15,
      |          "low" : 90.01,
      |          "low52" : 61.77,
      |          "open" : 91.8,
      |          "openInterest" : 0,
      |          "optionStyle" : "",
      |          "optionUnderlier" : "",
      |          "previousClose" : 92.16,
      |          "previousDayVolume" : 5118556,
      |          "primaryExchange" : "NYSE",
      |          "symbolDescription" : "CLOUDFLARE INC CL A COM",
      |          "totalVolume" : 5272545,
      |          "upc" : 0,
      |          "cashDeliverable" : 0,
      |          "marketCap" : 25238318450.00,
      |          "sharesOutstanding" : 273883000,
      |          "nextEarningDate" : "02/10/2022",
      |          "beta" : 2.55,
      |          "yield" : 0.0,
      |          "declaredDividend" : 0.0,
      |          "dividendPayableDate" : 0,
      |          "pe" : 0.0,
      |          "week52LowDate" : 1615179986,
      |          "week52HiDate" : 1637211986,
      |          "intrinsicValue" : 0.0,
      |          "timePremium" : 0.0,
      |          "optionMultiplier" : 0.0,
      |          "contractSize" : 0.0,
      |          "expirationDate" : 0,
      |          "timeOfLastTrade" : 1646773800,
      |          "averageVolume" : 6501839,
      |          "ExtendedHourQuoteDetail" : {
      |            "lastPrice" : 92.0,
      |            "change" : -0.16,
      |            "percentChange" : -0.17,
      |            "bid" : 91.5,
      |            "bidSize" : 500,
      |            "ask" : 92.45,
      |            "askSize" : 100,
      |            "volume" : 5272545,
      |            "timeOfLastTrade" : 1646701052,
      |            "timeZone" : "",
      |            "quoteStatus" : "EH_BEFORE_OPEN"
      |          }
      |        },
      |        "Product" : {
      |          "symbol" : "NET",
      |          "securityType" : "EQ"
      |        }
      |      },
      |      {
      |        "dateTime" : "20:00:00 EST 03-07-2022",
      |        "dateTimeUTC" : 1646701200,
      |        "quoteStatus" : "CLOSING",
      |        "ahFlag" : "true",
      |        "MutualFund" : {
      |          "symbolDescription" : "VANGUARD INTERNATIONAL GROWTH INV",
      |          "cusip" : "921910204",
      |          "changeClose" : 0.08,
      |          "previousClose" : 32.88,
      |          "transactionFee" : "No Transaction Fee Found",
      |          "earlyRedemptionFee" : "0",
      |          "availability" : "Open to New Buy and Sell",
      |          "initialInvestment" : 3000.0,
      |          "subsequentInvestment" : 1.0,
      |          "fundFamily" : "VANGUARD",
      |          "fundName" : "VANGUARD INTERNATIONAL GROWTH INV",
      |          "changeClosePercentage" : 0.24,
      |          "timeOfLastTrade" : 1646701200,
      |          "netAssetValue" : 32.96,
      |          "publicOfferPrice" : 32.96,
      |          "netExpenseRatio" : 0.43,
      |          "grossExpenseRatio" : 0.43,
      |          "orderCutoffTime" : 1600,
      |          "salesCharge" : "None",
      |          "initialIraInvestment" : 3000.0,
      |          "subsequentIraInvestment" : 1.0,
      |          "fundInceptionDate" : 370670400,
      |          "averageAnnualReturns" : 0.0,
      |          "sevenDayCurrentYield" : 0.0,
      |          "annualTotalReturn" : 32.96,
      |          "weightedAverageMaturity" : 0.0,
      |          "averageAnnualReturn1Yr" : -0.8496,
      |          "averageAnnualReturn3Yr" : 27.6067,
      |          "averageAnnualReturn5Yr" : 21.0003,
      |          "averageAnnualReturn10Yr" : 13.8193,
      |          "high52" : 56.3,
      |          "low52" : 32.88,
      |          "week52LowDate" : 1646629586,
      |          "week52HiDate" : 1630987586,
      |          "exchangeName" : "US Mutual Fund Providers",
      |          "sinceInception" : 10.8178,
      |          "quarterlySinceInception" : 11.3104,
      |          "lastTrade" : 32.96,
      |          "exchangeCode" : "BETA",
      |          "NetAssets" : {
      |            "value" : 58789338540,
      |            "asOfDate" : 1643605200
      |          }
      |        },
      |        "Product" : {
      |          "symbol" : "VWIGX",
      |          "securityType" : "MF"
      |        }
      |      }
      |    ]
      |  }
      |}""".stripMargin

  test("Moving Top Pointer") {

    io.circe.parser.parse(jsonTxt) match {
      case Left(value)  =>
        scribe.error(s"Could not parse JSON", value)
        throw IllegalStateException()
      case Right(value) =>
        scribe.info(s"Got JSON: ${value.spaces4}")
        val dec = value.as[FOOFOO]
        scribe.info(s"Decoding: ${dec}")
    }

  }

}

case class FOOFOO(quotes: Any)
object FOOFOO {
  import io.circe.Decoder.*

  given decoder: Decoder[FOOFOO] = {

    val nestDecoder: Decoder[List[Quote]] = decodeList[Quote].prepare(ac => ac.downField("QuoteResponse").downField("QuoteData"))
    // .at("QuoteResponse")" +      QuoteResponse".at("QuoteData")    // " +      QuoteData"Or Prepare

    nestDecoder.map(c => FOOFOO(c))
  }
}
