package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.{Account, Alert, ETOrder, ETimestamp, Messages}
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.*
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.syntax.*

case class ListAlertDetailsRs(alertDetailsResponse: AlertDetailsResponse)

object ListAlertDetailsRs:
  private val intCodec            = deriveCodec[ListAlertDetailsRs]
  given Codec[ListAlertDetailsRs] = CirceUtils.capitalizeCodec(intCodec)

case class AlertDetailsResponse(
    id: Long,
    createTime: ETimestamp,
    subject: String,
    msgText: String,
    readTime: Option[ETimestamp],
    deleteTime: Option[ETimestamp],
    symbol: Option[String],
    next: Option[String],
    prev: Option[String]
) derives Codec.AsObject
