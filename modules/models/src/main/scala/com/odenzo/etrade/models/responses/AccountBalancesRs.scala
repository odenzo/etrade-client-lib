package com.odenzo.etrade.models.responses

import com.odenzo.base.CirceUtils
import com.odenzo.etrade.client.models.Computed
import com.odenzo.etrade.models.{Cash, Computed}
import io.circe.Decoder.Result
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

case class AccountBalanceRs(BalanceResponse: AccountBalances) derives

object AccountBalanceRs {
  private implicit val config: Configuration      = Configuration.default
  implicit val decoder: Decoder[AccountBalanceRs] = deriveConfiguredDecoder[AccountBalanceRs]

}

case class AccountBalances(
    accountId: String,
    accountType: String,
    optionLevel: String,
    accountDescription: String,
    quoteMode: Int,
    dayTraderStatus: String,
    accountMode: String,
    Cash: Cash,
    Computed: Computed
)

object AccountBalances {

  implicit val config: Configuration             = CirceUtils.customMemberConfig(Map("cash" -> "Cash", "computed" -> "Computed"))
  implicit val decoder: Decoder[AccountBalances] = deriveConfiguredDecoder[AccountBalances]

}
