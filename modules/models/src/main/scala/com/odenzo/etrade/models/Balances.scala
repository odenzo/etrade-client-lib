package com.odenzo.etrade.models

import io.circe.*

case class OpenCalls(minEquityCall: BigDecimal, fedCall: BigDecimal, cashCall: BigDecimal, houseCall: BigDecimal)
    derives Codec.AsObject

case class RealTimeValues(totalAccountValue: BigDecimal, netMv: BigDecimal, netMvLong: BigDecimal, netMvShort: BigDecimal)
    derives Codec.AsObject

case class Cash(fundsForOpenOrdersCash: BigDecimal, moneyMktBalance: BigDecimal) derives Codec.AsObject
