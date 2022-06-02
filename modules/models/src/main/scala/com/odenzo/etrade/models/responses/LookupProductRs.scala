package com.odenzo.etrade.models.responses

import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.utils.CirceUtils

import io.circe.*
import io.circe.Decoder.Result
import io.circe.Encoder.*
import io.circe.generic.semiauto.deriveCodec
import io.circe.syntax.*

case class LookupProductRs(messages: Option[Messages], data: List[ProductData]) {
  def isEmpty = data.isEmpty
}
object LookupProductRs                                                          {
  given codec: Codec.AsObject[LookupProductRs] = CirceUtils.nestedCapitalizeCodec(deriveCodec[LookupProductRs], "LookupResponse")
}

/**
  * One of the results of looking up a product (e.g. search for stock info) tipe might be able to be a enum, not sure. EQUITY is the common
  * one. And MUTUAL_FUND, guessing OPTION too.
  */
case class ProductData(symbol: String, description: String, tipe: String)

object ProductData:

  given codec: Codec.AsObject[ProductData] = CirceUtils.renamingCodec(deriveCodec[ProductData], Map("tipe" -> "type"))
