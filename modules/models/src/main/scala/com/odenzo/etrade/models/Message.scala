package com.odenzo.etrade.models

import com.odenzo.base.CirceUtils
import com.odenzo.etrade.models.responses.TransactionListRs
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class Messages(message: List[Message])

object Messages:
  given Codec.AsObject[Messages] = CirceUtils.capitalizeCodec(deriveCodec[Messages])

case class Message(description: String, code: Int, `type`: String) derives Codec.AsObject {
  def tipe: String     = `type`
  def isError: Boolean = tipe == "ERROR"
}
/*
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
 */
