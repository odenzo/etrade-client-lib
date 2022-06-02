package com.odenzo.etrade.api.commands.experimental

import com.odenzo.etrade.api.commands.{*, given}
import com.odenzo.etrade.api.requests.experiment.*
import com.odenzo.etrade.models.Account
import io.circe.*
import io.circe.Encoder.*
import io.circe.syntax.{*, given}

class ETradeCmdV2Test extends munit.FunSuite {
  val lookupZoo: LookupProductCmdV2 = LookupProductCmdV2("zzzzoo")

  def tester[T <: ETradeCmdV2](cmd: T)(using dec: Decoder[T], enc: Encoder[T], encObj: Encoder.AsObject[T]) = {
    val base: T          = cmd
    val trt: ETradeCmdV2 = cmd

    val baseJson = base.asJson
    val trtJson  = trt.asJson

    scribe.info(s"Cmd: $cmd\nJSON:${cmd.asJson}\nJO:\n${cmd.asJsonObject.asJson}")
    scribe.info(s"Cmd: $cmd\nJSON:${trt.asJson}\nJO:\n${trt.asJsonObject.asJson}")
    assertEquals(base.asJson, trt.asJson)
    assertEquals(base.asJsonObject, trt.asJsonObject)

    val backBase  = baseJson.as[T]
    val backTrait = baseJson.as[ETradeCmdV2]

    scribe.info(s"Back: ${pprint(backBase)}")
    scribe.info(s"Back: ${pprint(backTrait)}")
    assert(backBase.isRight)
    assertEquals(backBase, backTrait)
  }

  test("lookupProduct") {
    tester(LookupProductCmdV2("voodoo"))
  }
  test("listAccounts") {
    tester(ListAccountsCmdV2())
  }
}
