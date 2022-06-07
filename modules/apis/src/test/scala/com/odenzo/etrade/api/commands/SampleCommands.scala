package com.odenzo.etrade.api.commands

import cats.effect.IO
import com.odenzo.etrade.api.ETradeContext
import com.odenzo.etrade.api.requests.ETradeCmd
import com.odenzo.etrade.api.requests.{FetchAccountBalancesCmd, ETradeCmd, ListAccountsCmd, ListTransactionsCmd}
import com.odenzo.etrade.models.{Account, ETimestamp}
import org.http4s.*
import org.http4s.client.Client
import org.http4s.syntax.all.*
import org.scalacheck.Gen

/** Sample commands which may have non-sensical values, not non-integration testing. */
object SampleCommands {

  val genAccountId: Gen[String]    = Gen.alphaNumStr.map(a => a)
  val genAccountIdKey: Gen[String] = Gen.alphaNumStr.map(a => a)

  val genAccount: Gen[Account] =
    for {
      acctId     <- Gen.alphaNumStr
      acctKey    <- Gen.alphaNumStr
      mode       <- Gen.oneOf("ModeA", "ModeB", "ModeC")
      desc       <- Gen.alphaNumStr
      name       <- Gen.alphaNumStr
      acctType   <- Gen.alphaNumStr
      instType   <- Gen.alphaNumStr
      acctStatus <- Gen.alphaNumStr
      closed     <- Gen.const(ETimestamp.ZERO)
    } yield Account(None, acctId, acctKey, mode, desc, name, acctType, instType: String, acctStatus: String, closed)

  val genListTransactionCmd: Gen[ListTransactionsCmd] =
    for {
      key   <- genAccountIdKey
      count <- Gen.chooseNum(1, 50)
    } yield ListTransactionsCmd(key, None, None, count)

  val accountsCmd: ListAccountsCmd      = ListAccountsCmd()
  val balances: FetchAccountBalancesCmd = FetchAccountBalancesCmd("key", None, "BROKERAGE")

  val tupledAll: (ListAccountsCmd, FetchAccountBalancesCmd) = (accountsCmd, balances)

  val listAll: List[ETradeCmd] = List(accountsCmd, balances)

  given context: ETradeContext = ETradeContext(uri"/testing")
  given clientIO: Client[IO]   = null
}
