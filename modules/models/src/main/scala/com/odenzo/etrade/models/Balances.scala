package com.odenzo.etrade.models

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class OpenCalls(minEquityCall: BigDecimal, fedCall: BigDecimal, cashCall: BigDecimal, houseCall: BigDecimal)

object OpenCalls {
  implicit val codec: Codec.AsObject[OpenCalls] = deriveCodec[OpenCalls]
}

case class RealTimeValues(totalAccountValue: BigDecimal, netMv: BigDecimal, netMvLong: BigDecimal, netMvShort: BigDecimal)

object RealTimeValues {
  implicit val codec: Codec.AsObject[RealTimeValues] = deriveCodec[RealTimeValues]
}

case class Cash(fundsForOpenOrdersCash: BigDecimal, moneyMktBalance: BigDecimal)
object Cash {
  implicit val codec: Codec.AsObject[Cash] = deriveCodec[Cash]
}
