package com.odenzo.etrade.models.responses
import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.base.CirceUtils
import com.odenzo.etrade.models.*
import io.circe.*
import io.circe.Encoder.*
import io.circe.syntax.*
import io.circe.generic.semiauto.deriveCodec
import io.circe.Decoder.Result

/**
  * This decodes the Messages when the hcursor is at its position in respone, which varies with each response, { "specificRespone":
  * {"Messages": {}, "data": {}} style So, to apply seperately we look for <star>/Messages path
  */
case class MessageRs(messages: List[Message]) {
  def isEmpty = messages.isEmpty
}
object MessageRs:
  val decoder = Decoder.instance[MessageRs] { hc =>
    val messages: Result[List[Message]] =
      hc.keys.map(_.toList).getOrElse(List.empty) match {
        case List(one) => hc.downField(one).downField("Messages").downField("Message").as[List[Message]]
        case s         => DecodingFailure(s"${s.length} Fields - Expected 1", hc.history).asLeft
      }
    messages.map(MessageRs(_))
  }

  /** Note the encoder and decoder and not symmetric since we are navigating through an unknown intermediate. Kinda slack but... */
  val encoder = Encoder
    .encodeList[Message]
    .contramap[MessageRs](_.messages)
    .mapJson { (j: Json) => JsonObject("Messages" := JsonObject.singleton("Message", j)).asJson }

  given Codec[MessageRs] = Codec.from(decoder, encoder)

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
