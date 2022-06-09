package com.odenzo.etrade.models

import com.odenzo.etrade.models.*

import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

/**
  * Read-Only mostly used. See orderwriting package for Detail creation when placing and reviewing new orders. This is Detail object for
  * Orders, used for the return objects that are populated by E-Trade
  */
case class Detail(
    orderNumber: Option[Long],
    accountId: Option[String],
    previewTime: Option[ETimestamp],
    placedTime: Option[ETimestamp],
    executedTime: Option[ETimestamp],
    orderValue: Amount,
    status: OrderStatus,
    orderType: Option[OrderType],
    orderTerm: OrderTerm,
    priceType: Option[OrderPricingType],
    priceValue: Option[String],
    limitPrice: Option[Amount],
    stopPrice: Option[Amount],
    soptLimitPrice: Option[Amount],
    offsetType: Option[OrderOffsetType],
    offsetValue: Option[Amount],
    marketSession: MarketSession,
    routingDestination: Option[OrderRouting],
    bracketedLimitPrice: Option[Amount],
    initialStopPrice: Option[Amount],
    trailPrice: Option[BigDecimal],         // Amount | Percentage, // Depends on order type, lets try this instead of either
    triggerPrice: Option[Amount],
    conditionPrice: Option[Amount],
    conditionSymbol: Option[String],
    conditionType: Option[OrderConditionType],
    conditionFollowPrice: Option[OrderFollowPriceType],
    conditionSecurityType: Option[String],
    replacedByOrderId: Option[Long],
    replacesOrderId: Option[Long],
    allOrNone: Boolean,
    previewId: Option[Long],
    instrument: List[Instrument],
    messages: Option[Messages],
    investmentAmount: Option[Amount],
    positionQuantity: Option[PositionType],
    aipFlag: Option[Boolean],
    reInvestOption: Option[ReinvestOption], // For mutual funds
    estimatedCommission: Option[Amount],
    estimatedFees: Option[Amount],
    estimatedTotalAmount: Option[Amount],
    netPrice: Option[Amount],
    netBid: Option[Amount],
    netAsk: Option[Amount],
    gcd: Option[Long],                      // Greatest Common Denominators?
    ratio: Option[String],
    mfpriceType: Option[String],
    egQual: Option[ExecutionGuarantee]
)

object Detail:
  given Codec[Detail] = CirceUtils.renamingCodec[Detail](deriveCodec, Map("instrument" -> "Instrument"))
