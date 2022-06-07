package com.odenzo.etrade.models

import cats.data.Chain
import com.odenzo.etrade.models.utils.CirceCodecs.*
import io.circe.*
import io.circe.generic.semiauto.deriveCodec
import io.circe.Decoder.*
import io.circe.Encoder.*
import io.circe.Codec.*

import scala.util.Try

/** In scope of models.* these are a bunch of simple case classes with auto-derivation */

case class NetAssets(value: Long, asOfDate: ETimestamp) derives Codec.AsObject

case class Values(
    low: String,    //	string	When the dollar amount of mutual fund purchases reaches a specified level, the sales load decreases. This field stores the minimum investment level at which the discount becomes available.
    high: String,   //	string	The maximum investment level at which the discount becomes available
    percent: String //	string	The sales load percentage for amounts between the low and high values
) derives Codec.AsObject

/** E-Treade Catetory  - forget what this i. */
case class Category(categoryId: String, parentId: String, categoryName: String, parentName: String) derives Codec.AsObject

case class SalesChargeValues(lowhigh: String, percent: BigDecimal) derives Codec.AsObject

type Amount  = BigDecimal
type Percent = BigDecimal
