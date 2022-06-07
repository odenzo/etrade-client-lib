package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.utils.CirceUtils
import com.odenzo.etrade.models.{ETimestamp, Messages}
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class CancelOrderRs(cancelOrderResponse: CancelOrderResponse)
object CancelOrderRs:
  given Codec.AsObject[CancelOrderRs] = CirceUtils.capitalizeCodec(deriveCodec)

case class CancelOrderResponse(
    accountId: String,         //	string	The numeric account ID for the cancelled order
    orderId: Long,             //	integer (int64)	The order ID
    cancelTime: ETimestamp,    //	integer (int64)	The time, in Epoch time, that the cancel request was submitted
    messages: Option[Messages] //	The messages relating to the order cancellation
) derives Codec.AsObject
