package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.{Account, ETOrder, Messages}
import com.odenzo.etrade.models.utils.{CirceCodecs, CirceUtils}
import io.circe.*
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.syntax.*

case class ListOrdersRs(ordersResponse: OrdersResponse)
object ListOrdersRs:
  given Codec.AsObject[ListOrdersRs] = CirceUtils.capitalizeCodec(deriveCodec)

case class OrdersResponse(marker: Option[String], next: String, order: List[ETOrder], messages: Option[Messages])

object OrdersResponse:
  given Codec[OrdersResponse] = CirceCodecs.renamingCodec[OrdersResponse](Map("order" -> "Order"))
