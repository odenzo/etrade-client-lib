package com.odenzo.etrade.models

import io.circe.{Codec, JsonObject}
import io.circe.generic.AutoDerivation
import io.circe.generic.semiauto.deriveCodec
import io.circe.pointer.Pointer.Relative.Result.Json

/** See if we can get standard typeCode, securtyType and also for the lookupProduct productData */

case class ProductId(symbol: String, typeCode: Option[String]) derives Codec.AsObject
