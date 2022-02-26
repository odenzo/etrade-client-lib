package com.odenzo.etrade.models

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class ProductId(symbol: String, typeCode: String) // MUTUAL_FUND/EQUITY...

object ProductId {
  implicit val codec: Codec.AsObject[ProductId] = deriveCodec[ProductId]
}
