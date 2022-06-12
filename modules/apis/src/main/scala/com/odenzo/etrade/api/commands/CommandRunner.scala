package com.odenzo.etrade.api.commands

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.api.{ETradeContext, ETradeService}
import com.odenzo.etrade.api.requests.{AccountsApi, ETradeCmd, MarketApi}

import com.odenzo.etrade.api.requests.*
import com.odenzo.etrade.models.responses.*
import com.odenzo.etrade.models.Transaction
import com.odenzo.etrade.models.{*, given}
import io.circe.*
import io.circe.Codec.*
import io.circe.Encoder.*
import io.circe.Decoder.*
import io.circe.syntax.*
import org.http4s.client.Client

/**
  * We use TypeClass here because the action is different based on the type A. The goal here is to have different execution based on imports
  * different set of TC implementations, one set for back-end, one set for front end. This is for back-end, or direct usage of APIs without
  * proxying from front to back. Can also be used in front=end if CORS not an issue, so its left in this cross plaform area. You could also
  * make another set with more debugging etc. Would be nice to just define the Worker and the ETradeCmd and make sure the tupled parametes
  * from command match the tuple of the Worker. For another day. This is really "shape matching" instead of type matching. Could move to
  * type matching by making the *App(x,y,z) just *App(someCmd)
  *
  * WTF: This should allow be to say val x:ETradeService[ListAccountsRs] = ListAccountsCmd().exec even when there are no ContextFunction
  * this. Hmm, if we have a default action (we don't as encoded now, but most App as the same standard(...) then can we use import
  * scala.compiletime.summonFrom
  *
  * inline def setFor[T]: Set[T] = summonFrom { case ord: Ordering[T] => new TreeSet[T]()(using ord) case _ => new HashSet[T] } on the level
  * above this to check basically is there is a COmmandRunner then do this, else do default this. Meh, not really I think, since we have no
  * binding to making the request.
  */
trait CommandRunner[A <: ETradeCmd] {
  def fetch(a: A): ETradeService[a.RESULT]

  extension (a: A)
    def exec(): ETradeService[a.RESULT] = fetch(a)
}

type WORKER = (e: ETradeCmd) => e.RESULT

def fetch(a: ListAccountsCmd): ETradeService[a.RESULT]         = AccountsApi.listAccountsApp()
def fetch(a: FetchAccountBalancesCmd): ETradeService[a.RESULT] = AccountsApi.accountBalanceApp.tupled(Tuple.fromProductTyped(a))

given lac: CommandRunner[ListAccountsCmd] with
  override def fetch(a: ListAccountsCmd): ETradeService[ListAccountsRs] = AccountsApi.listAccountsApp()

given CommandRunner[FetchAccountBalancesCmd] with
  override def fetch(a: FetchAccountBalancesCmd): ETradeService[AccountBalanceRs] = AccountsApi
    .accountBalanceApp
    .tupled(Tuple.fromProductTyped(a))

given lcc: CommandRunner[ListTransactionsCmd] with
  override def fetch(a: ListTransactionsCmd): ETradeService[ListTransactionsRs] = AccountsApi
    .listTransactionsApp
    .tupled(Tuple.fromProductTyped(a))

given CommandRunner[FetchTxnDetailsCmd] with
  override def fetch(a: FetchTxnDetailsCmd): ETradeService[TransactionDetailsRs] = AccountsApi
    .transactionDetailsApp
    .tupled(Tuple.fromProductTyped(a))

given CommandRunner[ViewPortfolioCmd] with
  override def fetch(a: ViewPortfolioCmd): ETradeService[ViewPortfolioRs] = AccountsApi.viewPortfolioApp.tupled(Tuple.fromProductTyped(a))

given CommandRunner[LookupProductCmd] with
  override def fetch(a: LookupProductCmd): ETradeService[LookupProductRs] = MarketApi.lookUpProductApp(a.searchFragment)

given CommandRunner[FetchQuoteCmd] with
  override def fetch(a: FetchQuoteCmd): ETradeService[a.RESULT] = MarketApi.equityQuotesApp.tupled(Tuple.fromProductTyped(a))

given CommandRunner[GetOptionExpiryCmd] with
  override def fetch(a: GetOptionExpiryCmd): ETradeService[a.RESULT] = MarketApi.optionChainExpiryApp(a)

given CommandRunner[GetOptionChainsCmd] with
  override def fetch(a: GetOptionChainsCmd): ETradeService[a.RESULT] = MarketApi.optionChainsApp(a)

given CommandRunner[ListAlertsCmd] with
  override def fetch(a: ListAlertsCmd): ETradeService[a.RESULT] = AlertsApi.listAlertsApp(a)

given CommandRunner[ListAlertDetailsCmd] with
  override def fetch(a: ListAlertDetailsCmd): ETradeService[a.RESULT] = AlertsApi.listAlertDetailsApp(a)

given CommandRunner[DeleteAlertsCmd] with
  override def fetch(a: DeleteAlertsCmd): ETradeService[a.RESULT] = AlertsApi.deleteAlertApp(a)

given CommandRunner[ListOrdersCmd] with
  override def fetch(a: ListOrdersCmd): ETradeService[a.RESULT] = OrdersApi.listOrdersApp(a)

given CommandRunner[PreviewOrderCmd] with
  override def fetch(a: PreviewOrderCmd): ETradeService[a.RESULT] = OrdersApi.previewOrderApp(a)

given CommandRunner[PlaceOrderCmd] with
  override def fetch(a: PlaceOrderCmd): ETradeService[a.RESULT] = OrdersApi.placeOrderApp(a)

given CommandRunner[CancelOrderCmd] with
  override def fetch(a: CancelOrderCmd): ETradeService[a.RESULT] = OrdersApi.cancelOrderApp(a)

given CommandRunner[ChangePreviewedOrderCmd] with
  override def fetch(a: ChangePreviewedOrderCmd): ETradeService[a.RESULT] = OrdersApi.changePreviewedOrderApp(a)

given CommandRunner[PlaceChangedOrderCmd] with
  override def fetch(a: PlaceChangedOrderCmd): ETradeService[a.RESULT] = OrdersApi.placeChangedOrderApp(a)
