package com.odenzo.etrade.models

import io.circe.Codec

case class OrderId(orderId: Long, cashMargin: CashMargin = CashMargin.CASH)

case class ETOrder(
    orderId: Long,
    details: String,
    orderType: OrderType,
    totalOrderValue: Amount,
    totalCommision: Amount,
    orderDetail: List[OrderDetail],
    events: List[OrderEvent]
) derives Codec.AsObject

case class OrderDetail(
    orderNumber: Long,
    accountId: String,
    previewTime: ETimestamp,
    placedTime: ETimestamp,
    executedTime: ETimestamp,
    orderValue: Amount,
    status: OrderStatus,
    orderType: OrderType,
    orderTerm: OrderTerm,
    priceType: OrderPricingType,
    priceValue: String,
    limitPrice: Option[Amount],
    stopPrice: Option[Amount],
    soptLimitPrice: Option[Amount],
    offsetType: OrderOffsetType,
    offsetValue: Option[Amount],
    marketSession: MarketSession,
    routingDestination: OrderRouting,
    bracketedLimitPrice: Amount,
    initialStopPrice: Amount,
    trailPrice: BigDecimal,                 // Amount | Percentage, // Depends on order type, lets try this instead of either
    triggerPrice: Option[Amount],
    conditionPrice: Option[Amount],
    conditionSymbol: Option[String],
    conditionType: Option[OrderConditionType],
    conditionFollowPrice: Option[OrderFollowPriceType],
    conditionSecurityType: String,
    replacedByOrderId: Option[Long],
    replacesOrderId: Option[Long],
    allOrNone: Boolean,
    previewId: Option[Long],
    instrument: List[Instrument],
    messages: Option[Messages],
    investmentAmount: Option[Amount],
    positionQuantity: Option[PositionType],
    aipFlag: Boolean,
    reInvestOption: Option[ReinvestOption], // For mutual funds
    estimatedCommission: Option[Amount],
    estimatedFees: Option[Amount],
    estimatedTotalAmount: Option[Amount],
    netPrice: Option[Amount],
    netBid: Option[Amount],
    netAsk: Option[Amount],
    gcd: Option[Long],                      // Greatest Common Denominators?
    ratio: Option[String],
    mfpriceType: Option[String]
) derives Codec.AsObject

case class OrderEvent(name: OrderEventType, dateTime: ETimestamp, orderNumber: Long, instrumnet: List[Instrument])
    derives Codec.AsObject
