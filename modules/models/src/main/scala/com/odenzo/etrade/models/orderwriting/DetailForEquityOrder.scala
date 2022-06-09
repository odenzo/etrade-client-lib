package com.odenzo.etrade.models.orderwriting

import com.odenzo.etrade.models.{OrderTerm, *}
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec
//import io.circe.generic.semiauto.deriveCodec

/**
  * Read/Write portion of an OrderDetail for constructing new orders. Replies use full OrderDetail Going to make a few of these for
  * different kinds of orders, you can use OrderDetails to fully specify a scenario not covered.
  */
case class DetailForEquityOrder(
    accountId: String,
    marketSession: MarketSession,
    orderType: OrderType,
    orderTerm: OrderTerm,
    priceType: OrderPricingType,
    mfpriceType: Option[String] = None,
    priceValue: Option[String] = None,
    limitPrice: Option[Amount] = None,
    stopPrice: Option[Amount] = None,
    stopLimitPrice: Option[Amount] = None,
    offsetType: Option[OrderOffsetType] = None,
    offsetValue: Option[Amount] = None,
    routingDestination: OrderRouting = OrderRouting.AUTO,
    bracketedLimitPrice: Option[Amount] = None,
    initialStopPrice: Option[Amount] = None,
    trailPrice: Option[BigDecimal] = None,        // Amount | Percentage, // Depends on order type, lets try this instead of either
    triggerPrice: Option[Amount] = None,
    conditionPrice: Option[Amount] = None,
    conditionSymbol: Option[String] = None,
    conditionType: Option[OrderConditionType] = None,
    conditionFollowPrice: Option[OrderFollowPriceType] = None,
    conditionSecurityType: Option[String] = None,
    allOrNone: Boolean = false,
    instrument: List[InstrumentForOrder],
    //   positionQuantity: Option[PositionType],
    reInvestOption: Option[ReinvestOption] = None // For mutual funds or all?
    // Commented out below

    // replacedByOrderId: Option[Long],
    // replacesOrderId: Option[Long],

    // previewId: Option[Long], // I always preview before placing, so will get this back after previewing

    // previewTime: Option[ETimestamp] = None,
    // placedTime: Option[ETimestamp] = None,
    // executedTime: Option[ETimestamp] = None,
    // orderValue: Amount,
    // status: OrderStatus,
    // investmentAmount: Option[Amount],

    // aipFlag: Option[Boolean],

    //  gcd: Option[Long],                             // Greatest Common Denominators?
    // ratio: Option[String]
) //derives // Codec.AsObject //  (Inlines over 1024!)

object DetailForEquityOrder:
  private val base: Codec.AsObject[DetailForEquityOrder] = deriveCodec[DetailForEquityOrder]
  given Codec.AsObject[DetailForEquityOrder]             = CirceUtils.renamingCodec[DetailForEquityOrder](base, Map("instrument" -> "Instrument"))
