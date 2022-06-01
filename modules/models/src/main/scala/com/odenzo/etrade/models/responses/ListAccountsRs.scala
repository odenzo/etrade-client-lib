package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.Account
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.*
import io.circe.syntax.*

case class ListAccountsRs(accounts: List[Account])

object ListAccountsRs {

  /* Caution, only test for one account but goofy format, { AccountListResponse { Accounts { Account [ <AccountObj> ] .. */
  private val decoder: Decoder[ListAccountsRs] =
    new Decoder[ListAccountsRs] {
      final def apply(c: HCursor): Decoder.Result[ListAccountsRs] = {
        val l3 = c.downField("AccountListResponse").downField("Accounts").downField("Account")
        val la = l3.as[Vector[Account]]
        scribe.info(s"L3 Parsed: $la")
        la.map(v => new ListAccountsRs(v.toList))
      }
    }

  private val encoder: Encoder.AsObject[ListAccountsRs] = Encoder
    .AsObject
    .instance[ListAccountsRs] { rs =>
      val accountList: Json = rs.accounts.map(a => JsonObject.singleton("Account", a.asJson)).asJson
      val accounts          = JsonObject.singleton("Accounts", accountList)
      JsonObject.singleton("AccountListResponse", accounts.asJson)
    }

  given Codec.AsObject[ListAccountsRs] = Codec.AsObject.from(decoder, encoder)
}
