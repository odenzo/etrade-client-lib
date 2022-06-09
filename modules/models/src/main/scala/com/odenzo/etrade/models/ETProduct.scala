package com.odenzo.etrade.models

import io.circe.{Codec, JsonObject}
import io.circe.generic.AutoDerivation
import io.circe.generic.semiauto.deriveCodec
import io.circe.pointer.Pointer.Relative.Result.Json

case class ETProduct(symbol: Option[String], securityType: Option[SecutiyType])

object ETProduct {
  def apply(symbol: String, secutiyType: SecutiyType): ETProduct = ETProduct(Some(symbol), Some(secutiyType))
  given codec: Codec.AsObject[ETProduct]                         = deriveCodec[ETProduct]
}
