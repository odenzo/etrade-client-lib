package com.odenzo.etrade.api.commands

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.api.{ETradeContext, ETradeService}
import com.odenzo.etrade.api.requests.{MarketApi, AccountsApi}

import com.odenzo.etrade.api.commands.ETradeCmd.given
import com.odenzo.etrade.models.responses.*
import com.odenzo.etrade.models.Transaction
import com.odenzo.etrade.models.{given, *}
import io.circe.*
import io.circe.Codec.*
import io.circe.Encoder.*
import io.circe.Decoder.*
import io.circe.syntax.*
import org.http4s.client.Client

/** There can be a command runner for ANY command. This is not sealed. Thus we don't need */
trait CommandRunner[A <: ETradeCmd] {
  def fetch(a: A): ETradeService[a.RESULT]

  extension (a: A)
    def exec(): ETradeService[a.RESULT] = fetch(a)
}

type WORKER = (e: ETradeCmd) => e.RESULT

def fetch(a: ListAccountsCmd): ETradeService[a.RESULT]    = AccountsApi.listAccountsApp()
def fetch(a: AccountBalancesCmd): ETradeService[a.RESULT] = AccountsApi.accountBalanceApp.tupled(Tuple.fromProductTyped(a))

given lac: CommandRunner[ListAccountsCmd] with

  override def fetch(a: ListAccountsCmd): ETradeService[ListAccountsRs] = AccountsApi.listAccountsApp()

given CommandRunner[AccountBalancesCmd] with

  override def fetch(a: AccountBalancesCmd): ETradeService[AccountBalanceRs] = AccountsApi
    .accountBalanceApp
    .tupled(Tuple.fromProductTyped(a))

given lcc: CommandRunner[ListTransactionsCmd] with

  override def fetch(a: ListTransactionsCmd): ETradeService[ListTransactionsRs] = AccountsApi
    .listTransactionsApp
    .tupled(Tuple.fromProductTyped(a))

given CommandRunner[TransactionDetailsCmd] with

  override def fetch(a: TransactionDetailsCmd): ETradeService[TransactionDetailsRs] = AccountsApi
    .transactionDetailsApp
    .tupled(Tuple.fromProductTyped(a))

given CommandRunner[ViewPortfolioCmd] with
  override def fetch(a: ViewPortfolioCmd): ETradeService[ViewPortfolioRs] = AccountsApi.viewPortfolioApp.tupled(Tuple.fromProductTyped(a))

given CommandRunner[LookupProductCmd] with
  override def fetch(a: LookupProductCmd): ETradeService[LookupProductRs] = MarketApi.lookUpProductApp(a.searchFragment)

given CommandRunner[EquityQuoteCmd] with
  override def fetch(a: EquityQuoteCmd): ETradeService[a.RESULT] = MarketApi.equityQuotesApp.tupled(Tuple.fromProductTyped(a))