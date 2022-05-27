package com.odenzo.etrade.models

import io.circe.generic.*
import io.circe.*

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

  def isClosed: Boolean = closedDate.isDefined
}
