package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.*
import io.circe.*

import java.time.Instant

case class TransactionDetailsRs(
    transactionId: Long,
    accountId: String,
    transactionDate: Instant,
    postDate: Instant,
    amount: BigDecimal, // Includes Fees
    description: String,
    category: Category,
    brokerage: Brokerage
) derives Codec.AsObject
