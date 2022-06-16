package com.odenzo.etrade.models

import com.odenzo.etrade.models.utils.{CirceCodecs, CirceUtils}
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

/** ETrade Order renamed to avoid common clashed with Order. This is for responses. */
case class ETOrder(
    orderId: Long,
    details: String,
    orderType: OrderType,
    totalOrderValue: Option[Amount],
    totalCommision: Option[Amount],
    orderDetail: Option[List[Detail]],
    events: Option[List[OrderEvent]]
)

object ETOrder:
  given Codec[ETOrder] = CirceCodecs.renamingCodec(Map("orderDetail" -> "OrderDetail"))
