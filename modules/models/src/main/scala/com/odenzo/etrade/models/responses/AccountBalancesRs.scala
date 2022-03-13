package com.odenzo.etrade.models.responses

import com.odenzo.base.CirceUtils
import com.odenzo.etrade.models.*

import com.odenzo.etrade.models.responses.AccountBalances.rename
import io.circe.*
import io.circe.Codec.*
import io.circe.Decoder.Result
import io.circe.generic.semiauto.*

case class AccountBalanceRs(balanceResponse: AccountBalances)

object AccountBalanceRs:
  given codec: Codec.AsObject[AccountBalanceRs] = CirceUtils.capitalizeCodec(deriveCodec[AccountBalanceRs])

case class AccountBalances(
    accountId: String,
    ofOfDate: Option[ETimestamp], // UTC epoch time 64bit not there on realTimeNav at least.
    accountType: String,
    optionLevel: String,
    accountDescription: String,
    quoteMode: Option[Int],       // Enum
    dayTraderStatus: Option[String],
    accountMode: Option[String],  // Skipping ssome in the docs becauze dont care and don't show up on sandbox anyway
    cash: Cash,
    openCalls: Option[OpenCalls],
    margin: Option[Margin],
    lending: Option[Lending],
    computed: ComputedBalance
)

object AccountBalances {

  def rename                                   = Map("cash" -> "Cash", "computed" -> "Computed")
  given codec: Codec.AsObject[AccountBalances] = CirceUtils.renamingCodec(deriveCodec[AccountBalances], rename)

}
