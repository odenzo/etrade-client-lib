package com.odenzo.etrade.models.responses

import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.base.CirceUtils
import com.odenzo.etrade.models.*

import io.circe.*
import io.circe.Decoder.Result
import io.circe.Encoder.*
import io.circe.generic.semiauto.deriveCodec
import io.circe.syntax.*

/**
  * This decodes the Messages when the hcursor is at its position in respone, which varies with each response, { "specificRespone":
  * {"Messages": {}, "data": {}} style So, to apply seperately we look for <star>/Messages path
  */
case class LookUpProductRs(data: List[Data]) {
  def isEmpty = data.isEmpty
}
object LookUpProductRs                       {
  private val decoder = Decoder.instance[LookUpProductRs] { hc =>
    val messages: Result[List[Data]] =
      hc.keys.map(_.toList).getOrElse(List.empty) match {
        case List(one) => hc.downField(one).downField("Data").as[List[Data]]
        case s         => DecodingFailure(s"${s.length} Fields - Expected 1", hc.history).asLeft
      }
    messages.map(LookUpProductRs(_))
  }

  /** Note the encoder and decoder and not symmetric since we are navigating through an unknown intermediate. Kinda slack but... */
  private val encoder = Encoder
    .encodeList[Data]
    .contramap[LookUpProductRs](_.data)
    .mapJson { (j: Json) => JsonObject("LookupResponse" := JsonObject.singleton("Data", j)).asJson }

  given Codec[LookUpProductRs] = Codec.from(decoder, encoder)
}
/*
 {
  "QuoteResponse" : {
    "Messages" : {
      "Message" : [
        {
          "description" : "APPL is an invalid symbol",
          "code" : 1002,
          "type" : "ERROR"
        }
      ]
    }
  }
}
 */

case class Data(symbol: String, description: String, tipe: String)

object Data:
  import com.odenzo.etrade.models.codecs.given
  val rename                        = Map("tipe" -> "type")
  given codec: Codec.AsObject[Data] = CirceUtils.renamingCodec(deriveCodec[Data], rename)
