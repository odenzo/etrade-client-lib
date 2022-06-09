package com.odenzo.etrade.models

import io.circe.{Codec, JsonObject}
import io.circe.generic.AutoDerivation
import io.circe.generic.semiauto.deriveCodec
import io.circe.pointer.Pointer.Relative.Result.Json

/** See if we can get standard typeCode, securtyType and also for the lookupProduct productData */

/** This is returned in Portfolio views (with Complete at least) */
case class ProductFull(
    symbol: String,
    securityType: Option[SecutiyType],
    expiryYear: Int,         // Dunno, YYYY I guess, 0 for my non-option stuff
    expiryMonth: Int,
    expiryDay: Int,
    strikePrice: BigDecimal, // Option info
    productId: ProductId
) derives Codec.AsObject
