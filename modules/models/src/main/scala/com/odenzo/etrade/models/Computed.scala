package com.odenzo.etrade.models

import com.odenzo.base.CirceUtils
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

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
    OpenCalls: OpenCalls,
    RealTimeValues: RealTimeValues
)

object Computed {

  implicit val config: Configuration             = CirceUtils.customMemberConfig(Map("openCalls" -> "OpenCalls", "realTimeValues" -> "RealTimeValues"))
  implicit val decoder: Codec.AsObject[Computed] = deriveConfiguredCodec[Computed]
}
