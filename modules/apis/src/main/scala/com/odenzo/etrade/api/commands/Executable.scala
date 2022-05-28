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

trait Executable[A <: ETradeCmd, U: Encoder: Decoder] {
  extension (a: A)
    def exec(): ETradeService[U]
}

given listAccounts: Executable[ListAccountsCmd, List[Account]] with
  extension (a: ListAccountsCmd)
    override def exec(): ETradeService[List[Account]] = AccountsApi.listAccountsApp()

given acctBalances: Executable[AccountBalancesCmd, AccountBalanceRs] with
  extension (a: AccountBalancesCmd)
    override def exec(): ETradeService[AccountBalanceRs] = AccountsApi.accountBalanceApp.tupled(Tuple.fromProductTyped(a))

given listTxn: Executable[ListTransactionsCmd, Chain[Transaction]] with
  extension (a: ListTransactionsCmd)
    override def exec(): ETradeService[Chain[Transaction]] =
      val args = Tuple.fromProductTyped(a)
      AccountsApi.listTransactionsApp.tupled(args)

given txnDetails: Executable[TransactionDetailsCmd, TransactionDetailsRs] with
  extension (a: TransactionDetailsCmd)
    override def exec(): ETradeService[TransactionDetailsRs] = AccountsApi.transactionDetailsApp.tupled(Tuple.fromProductTyped(a))

given viewPortfolio: Executable[ViewPortfolioCmd, Chain[ViewPortfolioRs]] with
  extension (a: ViewPortfolioCmd)
    override def exec(): ETradeService[Chain[ViewPortfolioRs]] = AccountsApi.viewPortfolioApp.tupled(Tuple.fromProductTyped(a))

given lookupProduct: Executable[LookupProductCmd, LookUpProductRs] with
  extension (a: LookupProductCmd)
    override def exec(): ETradeService[LookUpProductRs] = MarketApi.lookUpProductApp(a.searchFragment)

given equityQuote: Executable[EquityQuoteCmd, QuoteRs] with
  extension (a: EquityQuoteCmd)
    override def exec(): ETradeService[QuoteRs] = MarketApi.equityQuotesApp.tupled(Tuple.fromProductTyped(a))
