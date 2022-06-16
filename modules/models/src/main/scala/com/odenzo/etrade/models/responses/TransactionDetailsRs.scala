package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.utils.{CirceCodecs, CirceUtils}
import io.circe.*

import java.time.Instant

case class TransactionDetailsRs(transactionDetailsResponse: TransactionDetailsResponse)

object TransactionDetailsRs:
  private val codec: Codec.AsObject[TransactionDetailsRs] = generic.semiauto.deriveCodec[TransactionDetailsRs]
  given Codec.AsObject[TransactionDetailsRs]              = CirceUtils.capitalizeCodec[TransactionDetailsRs](codec)

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

  given Codec.AsObject[TransactionDetailsResponse] = CirceCodecs.renamingCodec(
    Map(
      "category"  -> "Category",
      "brokerage" -> "Brokerage"
    )
  )
