package com.odenzo.etrade.models

import io.circe.{Codec, Decoder}

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
) derives Codec.AsObject
