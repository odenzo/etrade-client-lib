package com.odenzo.etrade.api.commands

import io.circe.*
import io.circe.syntax.given
import io.circe.syntax.*
class EnvTest extends munit.FunSuite {
  import com.odenzo.etrade.api.commands.*
  test("Codec") {
    val cmd: ListAccountsCmd = ListAccountsCmd()
    val json                 = cmd.asJson
    scribe.info(s"ListAccounts: ${json.spaces4}")
  }

  test("Disc") {
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

//    val xx: CmdEnvelope[ETradeCmd]     = CmdEnvelope(LookupProduct("bar"), "12")
//    val xy: CmdEnvelope[LookupProduct] = CmdEnvelope(LookupProduct("foo"), "12")
//
//    val back = xy.asJson.as[CmdEnvelope[LookupProduct]]
//    scribe.info(s"BACK: $back")
//    scribe.info(s"XX = ${xx.asJson.spaces4}")
//    scribe.info(s"XY = ${xy.asJson.spaces4}")
  }

  test("Hints") {
    import io.circe.Encoder.AsObject.*
    import Hints.*
    val x: Hints = Hints.SKIP
    val json     = x.asJson
    scribe.info(s"Enum: ${json.spaces4}")
  }

  test("Options") {
    val option: Options = Options.FOO
    scribe.info(s"Option: ${option.asJson.spaces4}")
  }

  test("Wrapper") {
    val x = Wrapper(Hints.SKIP, Options.HELLO)
    scribe.info(s"Wrapper: ${x.asJson.spaces4}")
  }
}
