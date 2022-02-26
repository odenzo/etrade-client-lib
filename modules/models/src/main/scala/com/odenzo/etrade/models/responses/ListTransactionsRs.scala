package com.odenzo.etrade.models.responses

import com.odenzo.base.CirceUtils
import com.odenzo.etrade.models.Transaction
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredDecoder, deriveConfiguredEncoder}

case class ListTransactionsRs(
    next: Option[String] = None,
    marker: Option[String] = None,
    pageMarkers: String,
    moreTransactions: Boolean,
    transactionCount: Long,
    totalCount: Long,
    transaction: Vector[Transaction] // Capilized in actual JSON
)

object ListTransactionsRs {

  implicit val config: Configuration = CirceUtils.customMemberConfig(Map("transaction" -> "Transaction"))

  implicit val enc: Encoder.AsObject[ListTransactionsRs] = deriveConfiguredEncoder[ListTransactionsRs]
  implicit val dec: Decoder[ListTransactionsRs]          = deriveConfiguredDecoder[ListTransactionsRs].at("TransactionListResponse")

}
