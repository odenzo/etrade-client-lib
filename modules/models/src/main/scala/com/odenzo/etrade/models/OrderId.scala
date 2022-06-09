package com.odenzo.etrade.models

import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class OrderId(orderId: Long, cashMargin: CashMargin = CashMargin.CASH) derives Codec.AsObject
