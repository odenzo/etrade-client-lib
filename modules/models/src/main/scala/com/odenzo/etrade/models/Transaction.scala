package com.odenzo.etrade.models

import io.circe.*

case class Transaction(
    transactionId: String,
    accountId: String,
    transactionDate: EDatestamp,
    postDate: EDatestamp,
    amount: BigDecimal,
    description: String,
    description2: Option[String],
    transactionType: String,    // e.g. Interest, Dividend
    memo: String,               // "" if empty, sometime \n\t\t
    imageFlag: Boolean,
    instType: Option[String],
    storeId: Long,
    detailsURI: Option[String], // Have seen one of thee yet
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
