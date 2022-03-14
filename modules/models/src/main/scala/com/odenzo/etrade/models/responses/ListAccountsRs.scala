package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.Account

import io.circe.{Decoder, HCursor, Json}

case class ListAccountsRs(accounts: List[Account])

object ListAccountsRs {

  implicit val decoder: Decoder[ListAccountsRs] =
    new Decoder[ListAccountsRs] {
      final def apply(c: HCursor): Decoder.Result[ListAccountsRs] = {
        val l3 = c.downField("AccountListResponse").downField("Accounts").downField("Account")
        val la = l3.as[Vector[Account]]
        scribe.info(s"L3 Parsed: $la")
        la.map(v => new ListAccountsRs(v.toList))
      }
    }

}
