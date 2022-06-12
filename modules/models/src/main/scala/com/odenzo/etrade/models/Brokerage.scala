package com.odenzo.etrade.models

import com.odenzo.etrade.models.responses.TransactionDetailsResponse
import com.odenzo.etrade.models.utils.{CirceCodecs, CirceUtils}
import io.circe.{Codec, Decoder, JsonObject, generic}

import java.time.Instant

case class Brokerage(
    product: Option[ETProduct],        // Some anamoly. need to upcase product => Product too
    quantity: BigDecimal,
    price: BigDecimal,
    settlementCurrency: String,        // ISO 3
    paymentCurrency: String,
    fee: BigDecimal,
    displaySymbol: Option[String],     // Again some quoted whitespace \n\t\t\t. Missing whenb project is empty ACH IN/Out
    settlementDate: Option[EDateStamp] // Txn seem to have this in different unit.
)

object Brokerage:
  private val codec: Codec.AsObject[Brokerage] = generic.semiauto.deriveCodec[Brokerage]
  given Codec.AsObject[Brokerage]              = CirceCodecs.renamingCodec(codec, Map("product" -> "Product"))
