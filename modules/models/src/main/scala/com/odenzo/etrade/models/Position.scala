package com.odenzo.etrade.models

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

import java.time.Instant

case class Position(
    id: Long,
    symbolDescription: String,
    dateAquired: Instant,
    pricePaid: BigDecimal,
    commissions: BigDecimal,
    otherFees: BigDecimal,
    quantity: BigDecimal,
    positionIndicator: String,
    positionType: String, // enum LONG, SHORT or whatever
    daysGain: BigDecimal,
    daysGainPct: BigDecimal,
    marketValue: BigDecimal,
    totalCost: BigDecimal,
    totalGain: BigDecimal,
    totalGainPct: BigDecimal,
    pctOfPortfolio: BigDecimal,
    costPerShare: BigDecimal,
    todayComissions: BigDecimal,
    todayFees: BigDecimal,
    todayPricePaid: BigDecimal,
    todayQuantity: BigDecimal,
    adjPrevClose: BigDecimal,
    lotsDetails: OUrl,
    quoteDetails: OUrl,
    product: ETProduct,
    quick: Quick
)

object Position {
  implicit val codec: Codec.AsObject[Position] = deriveCodec[Position]
}

//
//case class Position(
//    positionId: Int,
//    symbolDescription: String,
//    dateAcquired: Instant,
//    pricePaid: BigDecimal,
//    commissions: BigDecimal,
//    otherFees: BigDecimal,
//    quantity: BigDecimal,
//    positionIndicator: String,
//    positionType: String,
//    daysGain: BigDecimal,
//    daysGainPct: BigDecimal,
//    marketValue: BigDecimal,
//    totalCost: BigDecimal,
//    totalGain: BigDecimal,
//    totalGainPct: BigDecimal,
//    pctOfPortfolio: BigDecimal,
//    costPerShare: BigDecimal,
//    todayCommissions: BigDecimal,
//    todayFees: BigDecimal,
//    todayPricePaid: BigDecimal,
//    todayQuantity: BigDecimal,
//    adjPrevClose: BigDecimal,
//    lotsDetails: String,  // URL
//    quoteDetails: String, // URL https://api.etrade.com/v1/market/quote/AAPL,
//    product: Product,
//    quick: Quick
//)
