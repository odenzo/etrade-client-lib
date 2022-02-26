package com.odenzo.etrade.models

import io.circe.Codec
import io.circe.literal.JsonStringContext
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

case class Transaction(
    transactionId: String,
    accountId: String,
    transactionDate: Long,
    postDate: Long,
    amount: BigDecimal,
    description: String,     // Possibly enum doable
    transactionType: String, // e.g. Interest, Dividend
    memo: String,            // "" if empty
    imageFlag: Boolean,
    instType: String,
    storeId: Long,
    detailsURI: String,
    brokerage: Brokerage
)

object Transaction {
  //implicit val foo: Codec[Instant]                = implicitly[Codec[Instant]]
  implicit val config                             = Configuration.default
  implicit val codec: Codec.AsObject[Transaction] = deriveConfiguredCodec[Transaction]

  val testData = json"""  {
                               "transactionId" : "19028105855702",
                               "accountId" : "61737052",
                               "transactionDate" : 1548662400000,
                               "postDate" : 1548662400000,
                               "amount" : 0.03,
                               "description" : "EXTENDED INSURANCE SWEEP DEPOSIT ACCOUNT INTEREST",
                               "transactionType" : "Interest",
                               "memo" : "",
                               "imageFlag" : false,
                               "instType" : "BROKERAGE",
                               "storeId" : 3,
                               "brokerage" : {
                                 "product" : {
                                   "symbol" : "#2145605",
                                   "securityType" : "BOND"
                                 },
                                 "quantity" : 0,
                                 "price" : 0,
                                 "settlementCurrency" : "USD",
                                 "paymentCurrency" : "USD",
                                 "fee" : 0,
                                 "displaySymbol" : "#2145605",
                                 "settlementDate" : 1548662400000
                               },
                               "detailsURI" : "https://api.etrade.com/v1/accounts/cwrsjbzCmJsrSi0X2T4gyA/transactions/19028105855702?storeId=3"
                             }"""
}
