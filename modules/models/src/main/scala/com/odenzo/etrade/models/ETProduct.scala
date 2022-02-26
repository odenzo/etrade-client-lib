package com.odenzo.etrade.models

import io.circe.Codec
import io.circe.generic.AutoDerivation
import io.circe.generic.semiauto.deriveCodec

// Difference between ProductId?
case class ETProduct(symbol: String, securityType: String) extends AutoDerivation

object ETProduct {
  implicit val codec: Codec.AsObject[ETProduct] = deriveCodec[ETProduct]
}
case class Product2(
    symbol: String,
    securityType: String,
    expiryYear: BigDecimal, // Dunno, YYYY I guess, 0 for my non-option stuff
    expiryMonth: Int,
    expiryDay: Int,
    strikePrice: BigDecimal,
    productId: ProductId
)

object Product2 {
  implicit val codec: Codec.AsObject[Product2] = deriveCodec[Product2]
}