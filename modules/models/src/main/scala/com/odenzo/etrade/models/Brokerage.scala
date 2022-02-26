package com.odenzo.etrade.models

import io.circe.{Codec, Decoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

import java.time.Instant

case class Brokerage(
    product: ETProduct,
    quantity: BigDecimal,
    price: BigDecimal,
    settlementCurrency: String,
    paymentCurrency: String,
    fee: BigDecimal,
    displaySymbol: String,
    settlementDate: Long
)

object Brokerage {

  implicit val instantDc                        = implicitly[Decoder[Instant]]
  implicit val config: Configuration            = Configuration.default
  implicit val codec: Codec.AsObject[Brokerage] = deriveConfiguredCodec[Brokerage]
}
