package com.odenzo.etrade.models
import io.circe.*
case class PortfolioTotals(
    todaysGainLoss: BigDecimal,    //	Today's gain or loss
    todaysGainLossPct: BigDecimal, //	Today's gain or loss percentage
    totalMarketValue: BigDecimal,  //	Today's market value
    totalGainLoss: BigDecimal,     //	The total gain or loss
    totalGainLossPct: BigDecimal,  //	The total gain loss percentage
    totalPricePaid: BigDecimal,    //	The total price paid
    cashBalance: BigDecimal        //	The cash balance
) derives Codec.AsObject
