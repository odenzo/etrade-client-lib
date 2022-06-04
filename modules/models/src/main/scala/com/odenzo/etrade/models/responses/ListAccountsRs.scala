package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.Account
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.*
import io.circe.syntax.*

/** I only have one account so god knows what the multiple accounts looks like. Try sandbox someday. */
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
      val inAccounts: List[Account] = rs.accounts
      val accountsListJson          = inAccounts.asJson
      // Yes, one Account object has a list of one account !?
      val accountWithList           = JsonObject.singleton("Account", accountsListJson).asJson
      // This has no list, just a direct Account object, maybe different if I had two accounts? (how bizarre would that be!)
      val accounts                  = JsonObject.singleton("Accounts", accountWithList).asJson

      JsonObject.singleton("AccountListResponse", accounts)

    }

  given Codec.AsObject[ListAccountsRs] = Codec.AsObject.from(decoder, encoder)
}

/*
Actual  Body:

"""{
  "AccountListResponse":
    {"Accounts":
      {
        "Account":[
          {"accountId":"xxxx","accountIdKey":"xxx","accountMode":"CASH","accountDesc":"Individual Brokerage","accountName":" ","accountType":"INDIVIDUAL","institutionType":"BROKERAGE","accountStatus":"ACTIVE","closedDate":0,"shareWorksAccount":false}
          ]
      }
    }
  }"""

 */
