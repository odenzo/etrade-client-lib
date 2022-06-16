//package com.odenzo.etrade.oauth.server
//
//import scala.inline.*
//import scala.compiletime.*
//import scala.deriving.*
//import scala.quoted.*
//
//import com.odenzo.etrade.api.commands.CommandRunner
//import com.odenzo.etrade.api.requests.{
//  CancelOrderCmd,
//  ChangePreviewedOrderCmd,
//  DeleteAlertsCmd,
//  ETradeCmd,
//  FetchAccountBalancesCmd,
//  FetchQuoteCmd,
//  FetchTxnDetailsCmd,
//  GetOptionChainsCmd,
//  GetOptionExpiryCmd,
//  ListAccountsCmd,
//  ListAlertDetailsCmd,
//  ListAlertsCmd,
//  ListOrdersCmd,
//  ListTransactionsCmd,
//  LookupProductCmd,
//  PlaceChangedOrderCmd,
//  PlaceOrderCmd,
//  PreviewOrderCmd,
//  ViewPortfolioCmd
//}
//import cats.effect.IO
//
///** Experiment, moved to MacrosProject */
//object SubTypeApplied {
//
//  inline def applyToAll[T <: ETradeCmd](cmd: T) = {
//    // transperant and inlineSummonFrom too.
//    summonFrom {
//      case runner: CommandRunner[T] => IO(scribe.info(s"Running with $runner ${cmd}")) //  runner.fetch(cmd)
//      case _                        =>
//        // val typeDesc = Type.of[T]
//
//        error(s"No Command Runner for ${cmd.getClass}")
//    }
//  }
//
////  inline def actionSubType[T <: ETradeCmd](cmd: T)(op: T => IO[Unit]) = {
////
////    inline def action[T](t: T): IO[Unit] = op(t)
////
////    cmd match {
////      case c: ListAccountsCmd         => action(c)
////      case c: FetchAccountBalancesCmd => action(c)
////      case c: ListTransactionsCmd     => action(c)
////      case c: FetchTxnDetailsCmd      => action(c)
////      case c: FetchQuoteCmd           => action(c)
////      case c: LookupProductCmd        => action(c)
////      case c: ViewPortfolioCmd        => action(c)
////      case c: ListAlertsCmd           => action(c)
////      case c: ListAlertDetailsCmd     => action(c)
////      case c: DeleteAlertsCmd         => action(c)
////      case c: PreviewOrderCmd         => action(c)
////      case c: ListOrdersCmd           => action(c)
////      case c: PlaceOrderCmd           => action(c)
////      case c: CancelOrderCmd          => action(c)
////      case c: GetOptionExpiryCmd      => action(c)
////      case c: GetOptionChainsCmd      => action(c)
////      case c: PlaceChangedOrderCmd    => action(c)
////      case c: ChangePreviewedOrderCmd => action(c)
////
////    }
////  }
//}
