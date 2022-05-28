package com.odenzo.etrade.models

import io.circe.Codec
import io.circe.Decoder.*
import io.circe.Encoder.*
opaque type StoreId <: Long = Long
object StoreId:
  def apply(l: Long): StoreId = l: StoreId

given codecStoreId: Codec[StoreId] = Codec.from(decodeLong.map(l => StoreId(l)), encodeLong.contramap(_.toLong))
