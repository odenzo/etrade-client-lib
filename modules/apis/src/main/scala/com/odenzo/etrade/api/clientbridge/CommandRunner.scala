package com.odenzo.etrade.api.clientbridge

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.api.requests.*
import com.odenzo.etrade.api.{ETradeContext, ETradeService}
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.responses.*
import io.circe.*
import io.circe.Codec.*
import io.circe.Decoder.*
import io.circe.Encoder.*
import io.circe.syntax.*
import org.http4s.client.Client

/**
  * NO need for a typeclass in this instance, because we do the same action for all. Basically, serialize the command, call a webservice and
  * get a.RESULT type back. So, all the matching is done on the result type via adding an addition JSON field saying what type it is. See
  * CommandRoutes in the Server module for an example server side implementation.
  */
object CommandRunner:
  def fetch[A <: ETradeCmd](a: A)(using Encoder[A], Decoder[a.RESULT]): ETradeService[a.RESULT] = Proxy.send(a)
  extension [A <: ETradeCmd](a: A)
    def exec(using Encoder[A], Decoder[a.RESULT]): ETradeService[a.RESULT]                      = MyExt.fetch(a)
//
//given lac: CommandRunner[ListAccountsCmd] with
//  override def fetch(a: ListAccountsCmd): ETradeService[ListAccountsRs] = IO(scribe.info(s"Proxy Send")) *> Proxy.send(a)
//
//given CommandRunner[FetchAccountBalancesCmd] with
//  override def fetch(a: FetchAccountBalancesCmd): ETradeService[AccountBalanceRs] = Proxy.send[FetchAccountBalancesCmd](a)
//
//given lcc: CommandRunner[ListTransactionsCmd] with
//  override def fetch(a: ListTransactionsCmd): ETradeService[ListTransactionsRs] = Proxy.send[ListTransactionsCmd](a)
//
//given CommandRunner[FetchTxnDetailsCmd] with
//  override def fetch(a: FetchTxnDetailsCmd): ETradeService[a.RESULT] = Proxy.send(a)
//
//given CommandRunner[ViewPortfolioCmd] with
//  override def fetch(a: ViewPortfolioCmd): ETradeService[ViewPortfolioRs] = Proxy.send[ViewPortfolioCmd](a)
//
//given CommandRunner[LookupProductCmd] with
//  override def fetch(a: LookupProductCmd): ETradeService[LookupProductRs] = Proxy.send(a)
//
//given CommandRunner[FetchQuoteCmd] with
//  override def fetch(a: FetchQuoteCmd): ETradeService[a.RESULT] = Proxy.send[FetchQuoteCmd](a)
