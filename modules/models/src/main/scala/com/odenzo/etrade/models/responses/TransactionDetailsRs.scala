package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.*

import java.time.Instant

case class TransactionDetailsRs(transactionDetailsResponse: TransactionDetailsResponse)

object TransactionDetailsRs:

  given Decoder[TransactionDetailsRs] = Decoder[TransactionDetailsResponse]
    .map(res => TransactionDetailsRs(res))
    .at("TransactionDetailsResponse")

  given Encoder[TransactionDetailsRs] = Encoder
    .AsObject[TransactionDetailsResponse]
    .contramapObject[TransactionDetailsRs](rs => rs.transactionDetailsResponse)

case class TransactionDetailsResponse(
    transactionId: Long,
    accountId: String,
    transactionDate: EDateStamp,
    postDate: Option[EDateStamp],
    amount: BigDecimal, // Includes Fees
    description: String,
    category: Category,
    brokerage: Brokerage
)

object TransactionDetailsResponse:
  private val codec: Codec.AsObject[TransactionDetailsResponse] = generic.semiauto.deriveCodec[TransactionDetailsResponse]
  given Codec.AsObject[TransactionDetailsResponse]              = CirceUtils.renamingCodec(
    codec,
    Map(
      "category"  -> "Category",
      "brokerage" -> "Brokerage"
    )
  )
