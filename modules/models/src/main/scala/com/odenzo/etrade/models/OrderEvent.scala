package com.odenzo.etrade.models

import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class OrderEvent(name: OrderEventType, dateTime: ETimestamp, orderNumber: Long, instrumnet: List[Instrument])
    derives Codec.AsObject
