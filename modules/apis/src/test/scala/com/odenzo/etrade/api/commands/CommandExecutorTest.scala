package com.odenzo.etrade.api.commands

import io.circe.*
import io.circe.syntax.{*, given}
import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*

class CommandExecutorTest extends munit.FunSuite {

  import com.odenzo.etrade.api.commands.*

  object Bounds {
    val foo = "foo"
  }

  case class Foobar() {
    def name = "String"
  }

  test("GivenTypes") {
    import com.odenzo.etrade.api.commands.{given, *}
    import com.odenzo.etrade.api.commands.SampleCommands.*
    val l: CommandRunner[ListAccountsCmd] = com.odenzo.etrade.api.commands.lac
    val b                                 = Bounds
    val cc                                = Foobar()
    // So, given from trait is an object! ok
    scribe.info(s"LAC GIVEN: ${l}\n ${pprint(l.getClass.getTypeName)}")
    scribe.info(s"BOUNDS:  ${pprint(b)}")
  }

}
