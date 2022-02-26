package com.odenzo.etrade.models.responses

import com.odenzo.etrade.client.models.Category
import com.odenzo.etrade.models.{Brokerage, Category}
import io.circe.generic.AutoDerivation

import java.time.Instant

case class TreansactionDetailsRs(
    transactionId: Long,
    accountId: String,
    transactionDate: Instant,
    postDate: Instant,
    amount: BigDecimal, // Includes Fees
    description: String,
    category: Category,
    brokerage: Brokerage
) extends AutoDerivation
