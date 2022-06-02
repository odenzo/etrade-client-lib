package com.odenzo.etrade.models.responses

import cats.*
import cats.data.*
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.opaques.NEString
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.*
import io.circe.generic.semiauto.deriveCodec
import monocle.syntax.all.*
import javax.lang.model.element.NestingKind

case class ListTransactionsRs(transactionListResponse: TransactionListResponse) {
  def transactions: Chain[Transaction] = transactionListResponse.transaction
}
object ListTransactionsRs:
  given codec: Codec.AsObject[ListTransactionsRs] = CirceUtils.capitalizeCodec(deriveCodec[ListTransactionsRs])
  // Perhaps worth having a special case for this since its standard approach often and in etrade.
  given Semigroup[ListTransactionsRs] with
    def combine(a: ListTransactionsRs, b: ListTransactionsRs): ListTransactionsRs = a
      .focus(_.transactionListResponse.transaction)
      .modify(_ ++ b.transactionListResponse.transaction)

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

  given codec: Codec.AsObject[TransactionListResponse] = CirceUtils
    .renamingCodec(deriveCodec[TransactionListResponse], Map("transaction" -> "Transaction"))
