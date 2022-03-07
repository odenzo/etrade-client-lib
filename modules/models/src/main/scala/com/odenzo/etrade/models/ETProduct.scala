package com.odenzo.etrade.models

import io.circe.{Codec, JsonObject}
import io.circe.generic.AutoDerivation
import io.circe.generic.semiauto.deriveCodec
import io.circe.pointer.Pointer.Relative.Result.Json

/**
  * TODO: Codec dillema, if this is empty structure want to lift it. Maybe every symbol has a securityType I think. ETProduct[T] and
  * T[String] memebers, or an Scala 3 enume for ETProduct with ETNoProduct.type and ETProduct enums
  */
case class ETProduct(symbol: Option[String], securityType: Option[String])

object ETProduct {
  given codec: Codec.AsObject[ETProduct] = deriveCodec[ETProduct]

}
case class Product2(
    symbol: String,
    securityType: String,
    expiryYear: BigDecimal, // Dunno, YYYY I guess, 0 for my non-option stuff
    expiryMonth: Int,
    expiryDay: Int,
    strikePrice: BigDecimal,
    productId: ProductId
) derives Codec.AsObject
