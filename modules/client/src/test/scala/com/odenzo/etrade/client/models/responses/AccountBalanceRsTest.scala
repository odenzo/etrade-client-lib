package com.odenzo.etrade.client.models.responses
import com.odenzo.etrade.models.responses.AccountBalanceRs
import io.circe.Decoder.Result
import io.circe.pointer.Pointer.Relative.Result.Json
import munit.FunSuite

class AccountBalanceRsTest extends FunSuite {

  test("AccountBalanceRS") {
    // val foo: Result[AccountBalanceRs] =  .as[AccountBalanceRs]
    // scribe.info(s"Response: ${foo}")
  }

  def data = """{
                     "BalanceResponse" : {
                         "accountId" : "3333337056",
                         "accountType" : "CASH",
                         "optionLevel" : "NO_OPTIONS",
                         "accountDescription" : "XXXX W XXXX",
                         "quoteMode" : 0,
                         "dayTraderStatus" : "NO_PDT",
                         "accountMode" : "CASH",
                         "Cash" : {
                             "fundsForOpenOrdersCash" : 0,
                             "moneyMktBalance" : 94324567555.12
                         },
                         "Computed" : {
                             "cashAvailableForInvestment" : 66542297555.12,
                             "cashAvailableForWithdrawal" : 32146697555.12,
                             "totalAvailableForWithdrawal" : 2342397555.12,
                             "netCash" : 97555.12,
                             "cashBalance" : 0,
                             "settledCashForInvestment" : 3242397555.12,
                             "unSettledCashForInvestment" : 0,
                             "fundsWithheldFromPurchasePower" : 0,
                             "fundsWithheldFromWithdrawal" : 0,
                             "marginBuyingPower" : 0,
                             "cashBuyingPower" : 43297555.12,
                             "dtMarginBuyingPower" : 0,
                             "dtCashBuyingPower" : 0,
                             "shortAdjustBalance" : 0,
                             "accountBalance" : 0,
                             "OpenCalls" : {
                                 "minEquityCall" : 0,
                                 "fedCall" : 0,
                                 "cashCall" : 0,
                                 "houseCall" : 0
                             },
                             "RealTimeValues" : {
                                 "totalAccountValue" : 12345479552.7161502,
                                 "netMv" : 12345381997.5961502,
                                 "netMvLong" : 435456546381997.5961502,
                                 "netMvShort" : 0
                             }
                         }
                     }
                 }"""
}
