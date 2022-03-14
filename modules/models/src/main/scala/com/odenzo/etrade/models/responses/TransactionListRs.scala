package com.odenzo.etrade.models.responses

import cats.data.Chain
import com.odenzo.etrade.base.CirceUtils
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.opaques.NEString
import io.circe.*
import io.circe.generic.semiauto.deriveCodec

import javax.lang.model.element.NestingKind

case class TransactionListRs(transactionListResponse: TransactionListResponse)
object TransactionListRs:
  given codec: Codec.AsObject[TransactionListRs] = CirceUtils.capitalizeCodec(deriveCodec[TransactionListRs])
  // Perhaps worth having a special case for this since its standard approach often and in etrade.

case class TransactionListResponse(
    next: Option[String],           // Empty String to None, and field not present if no more scrolling
    marker: Option[String],         // Empty String to None, and now field not present if no more scrolling
    pageMarkers: Option[String],    // Looks like \n\t seperated markers, which includes one for the first page I guess.
    moreTransactions: Boolean,
    transactionCount: Long,
    totalCount: Option[Long],       // Only there when paging it seems
    transaction: Chain[Transaction] // Capilized in actual JSON, Always Non-Empty?
)

object TransactionListResponse:
  import com.odenzo.etrade.models.codecs.given
  val rename                                           = Map("transaction" -> "Transaction")
  given codec: Codec.AsObject[TransactionListResponse] = CirceUtils.renamingCodec(deriveCodec[TransactionListResponse], rename)
