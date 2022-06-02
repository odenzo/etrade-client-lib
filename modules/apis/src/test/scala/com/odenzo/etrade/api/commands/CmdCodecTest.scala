package com.odenzo.etrade.api.commands

import cats.Eq
import com.odenzo.etrade.api.commands.SampleCommands.*
import com.odenzo.etrade.api.commands.{*, given}
import com.odenzo.etrade.api.requests.{ListAccountsCmd, ListTransactionsCmd}
import com.odenzo.etrade.models.{Account, EDateStamp, ETimestamp}
import io.circe.*
import io.circe.syntax.{*, given}
import io.circe.testing.ArbitraryInstances
import org.scalacheck.{Arbitrary, Gen}

import scala.reflect.Typeable

class CmdCodecTest extends munit.FunSuite {

  object Implicits extends ArbitraryInstances {
    given Eq[ListAccountsCmd]        = Eq.fromUniversalEquals
    given Arbitrary[ListAccountsCmd] = Arbitrary { Gen.const(ListAccountsCmd()) }

    given Eq[ListTransactionsCmd]        = Eq.fromUniversalEquals
    given Arbitrary[ListTransactionsCmd] = Arbitrary { genListTransactionCmd }

  }

  test("ListAccountsCmd") {
    import Implicits.{given, *}
    import io.circe.testing.CodecTests
    val tests: CodecTests[ListAccountsCmd] = CodecTests[ListAccountsCmd]
    tests.codec
  }

  test("ListTransationCommand") {
    import Implicits.{given, *}
    import io.circe.testing.CodecTests
    val tests: CodecTests[ListTransactionsCmd] = CodecTests[ListTransactionsCmd]
    tests.codec
  }
}
