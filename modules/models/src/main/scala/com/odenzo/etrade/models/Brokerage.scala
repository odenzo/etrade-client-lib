package com.odenzo.etrade.models

import io.circe.{Codec, Decoder, JsonObject}

import java.time.Instant

case class Brokerage(
    product: JsonObject,           // This can be weird, including juts an product: { }
    quantity: BigDecimal,
    price: BigDecimal,
    settlementCurrency: String,    // ISO 3
    paymentCurrency: String,
    fee: BigDecimal,
    displaySymbol: Option[String], // Again some quoted whitespace \n\t\t\t. Missing whenb project is empty ACH IN/Out
    settlementDate: EDatestamp     // Txn seem to have this in different unit.
) derives Codec.AsObject
