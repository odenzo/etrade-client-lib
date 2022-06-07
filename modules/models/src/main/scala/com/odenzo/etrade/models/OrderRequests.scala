package com.odenzo.etrade.models

import io.circe.Codec

import java.util.UUID

/** Order Detail overly complex, need some builder assistant for order commands */
case class PreviewOrderRequest(
    orderType: OrderType,
    order: List[OrderDetail],
    clientOrderId: String
) derives Codec.AsObject

case class PlaceOrderRequest(orderType: OrderType, order: List[OrderDetail], clientOrderId: String, previewIds: List[PreviewId])
    derives Codec.AsObject

/** @param orderId Order confirmation Id for the order placed. */
case class CancelOrderRequest(orderId: Long) derives Codec.AsObject
