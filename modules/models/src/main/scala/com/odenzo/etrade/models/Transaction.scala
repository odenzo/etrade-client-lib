package com.odenzo.etrade.models

import io.circe.*

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
) derives Codec.AsObject

object Transaction {

  val testData = """  {
           "transactionId" : "123",
           "accountId" : "123",
           "transactionDate" : 123,
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
           "detailsURI" : "https://api.etrade.com/v1/accounts/cwrsjbz234CmJsrSi0X2T4gyA/transactions/234?storeId=3"
         }"""
}
