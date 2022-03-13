package com.odenzo.etrade.client.models.responses

import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.models.responses.TransactionListResponse
import io.circe.Decoder
import io.circe.generic.semiauto.deriveCodec

import java.time.Instant

class ListTransactionRsTest extends munit.CatsEffectSuite {
//
//  case class Foo(next: Option[String], totalCount: Long)
//  object Foo {
//    implicit val codec = deriveCodec[Foo].at("TransactionListResponse")
//
//  }
//  test("Manual Tests") {
//    val opjson = TxnData.txn.as[ListTransactionsRs](Decoder[ListTransactionsRs])
//    scribe.info(s"${oprint(opjson)}")
//
//  }
//
//  test("timestmp") {
//    val ts: Long         = 1548662400000L
//    scribe.info(s"<illies: ${Instant.ofEpochMilli(ts)}")
//    implicit val decoder = implicitly[Decoder[Instant]]
//
//  }

}

object TxnData {

  val txn = """{
  "TransactionListResponse" : {
    "pageMarkers" : "eNpTsAlITE91zi%2FNK%2FHMc04syi8tTs2xM7TRxybMpWATkl%2BSmBOUmpxflAKWBSnFEIOpg5sBV4UQAaoBcfxKc5NSi4JTC0tT85JT7Qx0DHQMgdgA4gA0WS4FiCbPFJ%2FMYpARClAB38Si7NQiO0NLAyMLQwNTC1NTcwOjmpoaiClQWS6YaoipME9CeSCz9ZENBwBanF0K",
    "moreTransactions" : false,
    "transactionCount" : 1,
    "totalCount" : 1,
    "Transaction" : [
      {
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
      }
    ]
  }
}
                           """
}
