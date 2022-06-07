package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.ETResult
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

case class DeleteAlertsRs(alertsResponse: DeleteAlertsResponse)
object DeleteAlertsRs:
  given Codec[DeleteAlertsRs] = CirceUtils.capitalizeCodec(deriveCodec[DeleteAlertsRs])

case class DeleteAlertsResponse(result: ETResult, failedAlerts: Option[List[Long]]) derives Codec.AsObject
