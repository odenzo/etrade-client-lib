package com.odenzo.etrade.models

import io.circe.generic.*
import io.circe.*

/** FIXME once calls working: https://apisb.etrade.com/docs/api/account/api-account-v1.html#/definitions/Account */
case class AccountListResponse(Account: Vector[Json]) derives Codec.AsObject

/**
  * @param intNo
  * @param accountId
  *   Encoded as String, but numeric
  * @param accountIdKey
  * @param accountMode
  * @param accountDesc
  * @param accountName
  * @param accountType
  * @param institutionType
  * @param accountStatus
  * @param closedDate
  *   Value of zero not closed. Convert to option
  */
case class Account(
    intNo: Option[Int],
    accountId: String,
    accountIdKey: String,
    accountMode: String,
    accountDesc: String,
    accountName: String,
    accountType: String,
    institutionType: String, // BROKERAGE
    accountStatus: String,   // ACTIVE/CLOSED
    closedDate: ETimestamp
) derives Codec.AsObject {

  def iClosed: Boolean = closedDate.isDefined
}
