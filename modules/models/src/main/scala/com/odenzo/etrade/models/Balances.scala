package com.odenzo.etrade.models

import com.odenzo.etrade.base.CirceUtils
import io.circe.*
import io.circe.generic.semiauto.deriveCodec

case class OpenCalls(minEquityCall: Option[BigDecimal], fedCall: Option[BigDecimal], cashCall: BigDecimal, houseCall: Option[BigDecimal])
    derives Codec.AsObject

/**
  * netMv could be Either'ed
  * @param totalAccountValue
  * @param netMv
  * @param netMvLong
  * @param netMvShort
  */
case class RealTimeValues(totalAccountValue: BigDecimal, netMv: BigDecimal, netMvLong: Option[BigDecimal], netMvShort: Option[BigDecimal])
    derives Codec.AsObject

case class Cash(fundsForOpenOrdersCash: BigDecimal, moneyMktBalance: BigDecimal) derives Codec.AsObject

case class Margin(dtCashOpenOrderReserve: BigDecimal, dtMarginOpenOrderReserver: BigDecimal) derives Codec.AsObject

case class Lending(
    currBalance: BigDecimal,
    creditLine: BigDecimal,
    outstandingBalance: BigDecimal,
    minPaymentDue: BigDecimal,
    amountPastDue: BigDecimal,
    availableCredit: BigDecimal,
    ytdInterestPaid: BigDecimal,
    lastYtdInterestPaid: BigDecimal,
    paymentDueDate: ETimestamp, // Or EDateStamp?
    lastPaymentReceivedDate: ETimestamp,
    paymentReceivedMtd: BigDecimal
) derives Codec.AsObject

case class ComputedBalance(
    cashAvailableForInvestment: BigDecimal,
    cashAvailableForWithdrawal: BigDecimal,
    totalAvailableForWithdrawal: Option[BigDecimal],
    netCash: BigDecimal,
    cashBalance: BigDecimal,
    settledCashForInvestment: BigDecimal,
    unSettledCashForInvestment: BigDecimal,
    fundsWithheldFromPurchasePower: BigDecimal,
    fundsWithheldFromWithdrawal: BigDecimal,
    marginBuyingPower: Option[BigDecimal],
    cashBuyingPower: Option[BigDecimal],
    dtMarginBuyingPower: Option[BigDecimal],
    dtCashBuyingPower: Option[BigDecimal],
    shortAdjustBalance: Option[BigDecimal],
    accountBalance: Option[BigDecimal],
    openCalls: OpenCalls,
    realTimeValues: RealTimeValues
)

object ComputedBalance {

  private def rename                    = Map("openCalls" -> "OpenCalls", "realTimeValues" -> "RealTimeValues")
  given Codec.AsObject[ComputedBalance] = CirceUtils.renamingCodec(deriveCodec[ComputedBalance], rename)

}
