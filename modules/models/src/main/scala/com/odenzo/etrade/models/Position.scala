package com.odenzo.etrade.models

import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
import org.http4s.Uri
//import org.http4s.circe.decodeUri

import java.time.Instant

case class Position(
    positionId: Long,
    symbolDescription: String,
    dateAcquired: EDateStamp,
    pricePaid: BigDecimal,
    commissions: BigDecimal,
    otherFees: BigDecimal,
    quantity: BigDecimal,
    positionIndicator: String,
    positionType: String,                 // enum LONG, SHORT or whatever
    daysGain: BigDecimal,
    daysGainPct: BigDecimal,
    marketValue: BigDecimal,
    totalCost: BigDecimal,
    totalGain: BigDecimal,
    totalGainPct: BigDecimal,
    pctOfPortfolio: BigDecimal,
    costPerShare: BigDecimal,
    todayCommissions: BigDecimal,
    todayFees: BigDecimal,
    todayPricePaid: BigDecimal,
    todayQuantity: BigDecimal,
    adjPrevClose: BigDecimal,
    lotsDetails: String,                  // uri has codecs but try to minimize pollution
    quoteDetails: String,
    product: ETProduct,
    quick: Option[QuickView],             // quick / complete /
    complete: Option[CompleteView],       // Performance, Fundamental, Option Views too. These are really Either[] or is there a oneOf since
    performance: Option[PerformanceView], // Performance, Fundamental, Option Views too. These are really Either[] or is there a oneOf
    fundamental: Option[FundamentalView]
    // since
    // they all have different keys
)

object Position:

  def rename: Map[String, String]       = List("product", "quick", "complete", "performance", "fundamental").map(lc => lc -> lc.capitalize).toMap
  given codec: Codec.AsObject[Position] = CirceUtils.renamingCodec(deriveCodec[Position], rename)
