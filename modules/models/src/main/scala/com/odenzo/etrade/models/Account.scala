package com.odenzo.etrade.models

import io.circe.generic.*
import io.circe.*

/** FIXME once calls working */
case class AccountListResponse(Account: Vector[Json]) derives Codec.AsObject

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
) derives Codec.AsObject
