package com.odenzo.etrade.models.responses

import cats.data.Chain
import com.odenzo.base.CirceUtils
import com.odenzo.etrade.models.*
import io.circe.*
import io.circe.generic.semiauto.deriveCodec

case class TransactionListRs(transactionListResponse: TransactionListResponse)
object TransactionListRs:
  given codec: Codec.AsObject[TransactionListRs] = CirceUtils.capitalizeCodec(deriveCodec[TransactionListRs])
  // Perhaps worth having a special case for this since its standard approach often and in etrade.

case class TransactionListResponse(
    next: Option[String] = None,    // Empty String to None
    marker: Option[String] = None,  // Empty String to None
    pageMarkers: Option[String],    // Looks like \n\t seperated markers, which includes one for the first page I guess.
    moreTransactions: Boolean,
    transactionCount: Long,
    totalCount: Long,
    transaction: Chain[Transaction] // Capilized in actual JSON, Always Non-Empty?
)

object TransactionListResponse:
  val rename                                           = Map("transaction" -> "Transaction")
  given codec: Codec.AsObject[TransactionListResponse] = CirceUtils.renamingCodec(deriveCodec[TransactionListResponse], rename)
