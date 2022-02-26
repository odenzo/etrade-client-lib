package com.odenzo.etrade.models.responses

import com.odenzo.base.CirceUtils
import com.odenzo.etrade.models.*
import io.circe.*
import io.circe.generic.semiauto.deriveCodec

case class ListTransactionsRs(
    next: Option[String] = None,
    marker: Option[String] = None,
    pageMarkers: String,
    moreTransactions: Boolean,
    transactionCount: Long,
    totalCount: Long,
    transaction: Vector[Transaction] // Capilized in actual JSON
)

object ListTransactionsRs:
  val rename                                      = Map("transaction" -> "Transaction")
  given codec: Codec.AsObject[ListTransactionsRs] = CirceUtils.renamingCodec(deriveCodec[ListTransactionsRs], rename)
