package com.odenzo.etrade.models

import com.odenzo.etrade.models.orderwriting.{DetailForEquityOrder, DetailForOrder}
import io.circe.*

import java.util.UUID

/** Order Detail overly complex, need some builder assistant for order commands */
case class PreviewOrderRequest(
    orderType: OrderType,
    order: List[DetailForEquityOrder],
    clientOrderId: String
) derives Codec.AsObject

case class PlaceOrderRequest(orderType: OrderType, order: List[DetailForEquityOrder], clientOrderId: String, previewIds: List[PreviewId])
    derives Codec.AsObject

/** @param orderId Order confirmation Id for the order placed. */
case class CancelOrderRequest(orderId: Long) derives Codec.AsObject

val enc: Encoder[PreviewOrderRequest] = Encoder.AsObject[PreviewOrderRequest].mapJson(_.deepDropNullValues)
