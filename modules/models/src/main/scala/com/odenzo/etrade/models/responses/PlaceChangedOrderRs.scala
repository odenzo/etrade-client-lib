package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.*
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.syntax.*

case class PlaceChangedOrderRs(placeOrderResponse: PlaceOrderResponse)
object PlaceChangedOrderRs:
  given Codec.AsObject[PlaceChangedOrderRs] = CirceUtils.capitalizeCodec(deriveCodec)
