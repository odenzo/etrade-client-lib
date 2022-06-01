package com.odenzo.etrade.api.commands

import cats.effect.IO
import com.odenzo.etrade.api.ETradeContext
import org.http4s.*
import org.http4s.client.Client
import org.http4s.syntax.all.*

object SampleCommands {

  val accountsCmd: ListAccountsCmd = ListAccountsCmd()
  val balances: AccountBalancesCmd = AccountBalancesCmd("key", None, "BROKERAGE")

  val tupledAll: (ListAccountsCmd, AccountBalancesCmd) = (accountsCmd, balances)

  val listAll: List[ETradeCmd] = List(accountsCmd, balances)

  given context: ETradeContext = ETradeContext(uri"/testing")
  given clientIO: Client[IO]   = null
}
