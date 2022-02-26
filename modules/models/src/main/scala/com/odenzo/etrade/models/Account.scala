package com.odenzo.etrade.models

import io.circe.generic.AutoDerivation
import io.circe._

case class AccountListResponse(Account: Vector[Json]) extends AutoDerivation

case class Account(
    accountId: String,
    accountIdKey: String,
    accountMode: String,
    accountDesc: String,
    accountName: String,
    accountType: String,
    institutionType: String, // BROKERAGE
    accountStatus: String,   // ACTIVE/CLOSED
    closedDate: Long
    //   instNo: Option[Int]      //
) extends AutoDerivation
