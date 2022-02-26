package com.odenzo.etrade.models.responses

import com.odenzo.base.CirceUtils
import com.odenzo.etrade.models.*
import io.circe.*
import io.circe.Codec.*
import io.circe.Decoder.Result
import io.circe.generic.semiauto.*

case class AccountBalanceRs(balanceResponse: AccountBalances) derives Codec.AsObject

case class AccountBalances(
    accountId: String,
    accountType: String,
    optionLevel: String,
    accountDescription: String,
    quoteMode: Int,
    dayTraderStatus: String,
    accountMode: String,
    cash: Cash,
    computed: Computed
)

object AccountBalances {

  def rename                                   = Map("cash" -> "Cash", "computed" -> "Computed")
  given codec: Codec.AsObject[AccountBalances] = CirceUtils.renamingCodec(deriveCodec[AccountBalances], rename)

}
