package com.odenzo.etrade.api.commands.progs

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.api.ETradeContext

import com.odenzo.etrade.api.commands.{*, given}
import com.odenzo.etrade.api.commands.*
import com.odenzo.etrade.api.commands.given

import com.odenzo.etrade.models.responses.*
import com.odenzo.etrade.models.*
import org.http4s.client.Client

class AccountsOverview(using Client[IO], ETradeContext) {

  def accounts(): IO[List[Account]] = {

    for {
      accounts <- ListAccountsCmd().exec()
      infos    <- accounts.traverse(acc => accountInfo(acc))
    } yield accounts
  }

  def accountInfo(account: Account): IO[Unit] = {
    val rtNav = true
    for {
      balances <- AccountBalancesCmd(account.accountIdKey, account.accountType.some, account.institutionType, rtNav).exec()
    } yield ()
  }
}
