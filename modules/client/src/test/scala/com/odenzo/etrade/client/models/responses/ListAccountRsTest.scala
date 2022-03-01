package com.odenzo.etrade.client.models.responses
import com.odenzo.etrade.models.Account
import com.odenzo.etrade.models.responses.ListAccountsRs

class ListAccountRsTest extends munit.CatsEffectSuite {

//  test("Get Account List as Json") {
//    val opjson = singleAccount.hcursor.downField("AccountListResponse").downField("Accounts").downField("Account").focus
//    scribe.info(s"${opjson}")
//    val res    = opjson.map(j => j.as[List[Account]])
//    scribe.info(s"$res")
//  }
//
//  test("Actual Decoder") {
//    val foo = ListAccountsRs.decoder.decodeJson(singleAccount)
//    scribe.info(s"$foo completed")
//  }

}

object Data {

  val singleAccount = """ {
                              "AccountListResponse" : {
                                "Accounts" : {
                                  "Account" : [
                                    {
                                      "accountId" : "xxxxx",
                                      "accountIdKey" : "cwrsjbzCmJsrSi0X2T4gyA",
                                      "accountMode" : "CASH",
                                      "accountDesc" : "Individual Brokerage",
                                      "accountName" : " ",
                                      "accountType" : "INDIVIDUAL",
                                      "institutionType" : "BROKERAGE",
                                      "accountStatus" : "ACTIVE",
                                      "closedDate" : 0
                                    }
                                  ]
                                }
                              }
                            }"""
}
