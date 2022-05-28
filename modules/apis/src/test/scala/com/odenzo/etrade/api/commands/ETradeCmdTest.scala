package com.odenzo.etrade.api.commands

import io.circe.*
import io.circe.syntax.{*, given}
import com.odenzo.etrade.api.commands.*
import com.odenzo.etrade.api.commands.ETradeCmd.*

class ETradeCmdTest extends munit.FunSuite {

  test("Specific asJson") {
    val cmd: ListAccountsCmd   = ListAccountsCmd()
    val cmdK: LookupProductCmd = LookupProductCmd("zoo")

    scribe.info(s"ListAccounts JSON>>\n${cmd.asJson.spaces4}")
    scribe.info(s"Lookup JSON>>\n${cmdK.asJson.spaces4}")

  }

  test("Specific asJsonObject") {
    val cmd: ListAccountsCmd                      = ListAccountsCmd()
    val cmdK: LookupProductCmd                    = LookupProductCmd("zoo")
    val asObj: Encoder.AsObject[LookupProductCmd] = summon[Encoder.AsObject[LookupProductCmd]]
    val asJson: Encoder[LookupProductCmd]         = summon[Encoder[LookupProductCmd]]

    val resO = asObj.encodeObject(cmdK)
    val res  = asObj.apply(cmdK)

    scribe.info(s"ListAccounts JSON>>\n${res.spaces4}")
    scribe.info(s"Lookup JSON>>\n${resO.asJson.spaces4}")

  }

  test("Trait") {
    val cmd: ETradeCmd  = ListAccountsCmd()
    val cmdK: ETradeCmd = LookupProductCmd("zoo")

    scribe.info(s"ListAccounts JSON>>\n${cmd.asJson.spaces4}")
    scribe.info(s"Lookup JSON>>\n${cmdK.asJson.spaces4}")

  }

  test("SpecificObjects") {
    val cmd: Json  = Encoder.AsObject[ListAccountsCmd].apply(ListAccountsCmd())
    val cmdK: Json = Encoder.AsObject[LookupProductCmd].apply(LookupProductCmd("zoo"))

    scribe.info(s"ListAccounts JSON>>\n${cmd.spaces4}")
    scribe.info(s"Lookup JSON>>\n${cmdK.spaces4}")

  }

  test("Widen") {
    // val cmd: ETradeCmd = LookupProduct("fppna")
    val cmd: ETradeCmd         = LookupProductCmd("zoo")
    val cmdK: LookupProductCmd = LookupProductCmd("zoo")
    val jj: Json               = cmd.asJson
    val kk: Json               = cmdK.asJson
    val ll: JsonObject         = Encoder.AsObject[LookupProductCmd].encodeObject(cmdK)
    val mm: Json               = Encoder[ETradeCmd].apply(cmd)

    scribe.info(s"Res JJ: ${jj.asJson.spaces4} ")
    scribe.info(s"Res KK: ${kk.asJson.spaces4} ")
    scribe.info(s"Res LL: ${ll.asJson.spaces4} ")
    scribe.info(s"Res MM: ${ll.asJson.spaces4} ")

  }

}
