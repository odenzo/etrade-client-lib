package com.odenzo.etrade.models

import io.circe.*

case class Transaction(
    transactionId: String,
    accountId: String,
    transactionDate: EDateStamp,
    postDate: EDateStamp,
    amount: BigDecimal,
    description: String,
    description2: Option[String],
    transactionType: String,    // e.g. Interest, Dividend
    memo: String,               // "" if empty, sometime \n\t\t
    imageFlag: Boolean,
    instType: Option[String],
    storeId: StoreId,
    detailsURI: Option[String], // Have seen one of thee yet
    brokerage: Brokerage
) derives Codec.AsObject {

  override def toString: String = pprint(this).render
}
