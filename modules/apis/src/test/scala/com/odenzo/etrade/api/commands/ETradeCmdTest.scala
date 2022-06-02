package com.odenzo.etrade.api.commands

import io.circe.*
import io.circe.syntax.{*, given}
import com.odenzo.etrade.api.commands.*
import com.odenzo.etrade.api.commands.given
import com.odenzo.etrade.api.requests.{ETradeCmd, ListAccountsCmd, LookupProductCmd}
import com.odenzo.etrade.models.Account
import io.circe.Encoder.*
import scala.reflect.Typeable

class ETradeCmdTest extends munit.FunSuite {
  val lookupZoo: LookupProductCmd = LookupProductCmd("zoo")

  test("ConcretetEncodeConcereteDecode") {
    val a: ETradeCmd = lookupZoo
    val json         = lookupZoo.asJson
    scribe.info(s"${pprint(a)} : ${json.spaces4}")
    val back         = json.as[LookupProductCmd]
    scribe.info(s"Back: ${pprint(back)}")
    assert(back.isRight)
  }

  test("ConcretetEncodeTraitDecode") {
    val a: ETradeCmd = lookupZoo
    val json         = lookupZoo.asJson
    scribe.info(s"${pprint(a)} : ${json.spaces4}")
    val back         = json.as[ETradeCmd]
    scribe.info(s"Back: ${pprint(back)}")
    assert(back.isRight)
  }

  test("TraitEncodeConcreteDecode") {
    val a: ETradeCmd = lookupZoo
    val json         = a.asJson
    scribe.info(s"${pprint(a)} : ${json.spaces4}")
    val back         = json.as[LookupProductCmd]
    scribe.info(s"Back: ${pprint(back)}")
    assert(back.isRight)
  }

  test("TraitEncodeTraitDecode") {
    val a: ETradeCmd = lookupZoo
    val json         = a.asJson
    scribe.info(s"${pprint(a)} : ${json.spaces4}")
    val back         = json.as[ETradeCmd]
    scribe.info(s"Back: ${pprint(back)}")
    assert(back.isRight)
  }

  test("Specific asJson") {

    val cmd: ListAccountsCmd   = ListAccountsCmd()
    val cmdK: LookupProductCmd = LookupProductCmd("zoo")

    val json    = cmd.asJson
    val jsonObj = cmd.asJsonObject

    val jsonK           = cmdK.asJson
    val joK             = cmdK.asJsonObject
    val first           = false
    val ecmd: ETradeCmd = if first then cmd else cmdK

    val jsonE = ecmd.asJson
    val joE   = ecmd.asJsonObject

    scribe.info(s"CMD:\n${json.spaces4} \n ${jsonObj.asJson.spaces4}")
    scribe.info(s"CMDK:\n${jsonK.spaces4} \n ${joK.asJson.spaces4}")
    scribe.info(s"ECMD:\n${jsonE.spaces4} \n ${joE.asJson.spaces4}")

    val back = jsonE.as[ETradeCmd]
    scribe.info(s"Back from $back")

    val foo: AnyVal                                = 123: Long
    def filterWhen[T: Typeable](a: Any): Option[T] =
      a match {
        case sa: T => Some(sa)
        case _     => Option.empty[T]
      }

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

  test("Encoder All") {}
}
