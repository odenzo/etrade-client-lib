package com.odenzo.etrade.models

import com.odenzo.base.CirceUtils
import com.odenzo.etrade.models.responses.AccountBalances
import io.circe.*
import io.circe.Codec
import io.circe.generic.AutoDerivation
import io.circe.generic.semiauto.deriveCodec

case class Computed(
    cashAvailableForInvestment: BigDecimal,
    cashAvailableForWithdrawal: BigDecimal,
    totalAvailableForWithdrawal: BigDecimal,
    netCash: BigDecimal,
    cashBalance: BigDecimal,
    settledCashForInvestment: BigDecimal,
    unSettledCashForInvestment: BigDecimal,
    fundsWithheldFromPurchasePower: BigDecimal,
    fundsWithheldFromWithdrawal: BigDecimal,
    marginBuyingPower: BigDecimal,
    cashBuyingPower: BigDecimal,
    dtMarginBuyingPower: BigDecimal,
    dtCashBuyingPower: BigDecimal,
    shortAdjustBalance: BigDecimal,
    accountBalance: BigDecimal,
    openCalls: OpenCalls,          // TODO: Normalize and Map to openCalls field name
    realTimeValues: RealTimeValues // TODO: realTimeValues field name
)

object Computed {

  def rename                     = Map("openCalls" -> "OpenCalls", "realTimeValue" -> "RealTimeValues")
  given Codec.AsObject[Computed] = CirceUtils.renamingCodec(deriveCodec[Computed], rename)

}
