package com.odenzo.etrade.models.responses

import cats.Semigroup
import com.odenzo.etrade.models.{Account, Alert, ETOrder, Messages}
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.*
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.syntax.*
import monocle.syntax.all.*

/** Note: This actually doesn't do paging. */
case class ListAlertsRs(alertsResponse: AlertsResponse)

object ListAlertsRs {

  given Codec[ListAlertsRs] = CirceUtils.capitalizeCodec(deriveCodec[ListAlertsRs])
  given Semigroup[ListAlertsRs] with
    def combine(a: ListAlertsRs, b: ListAlertsRs): ListAlertsRs = a.focus(_.alertsResponse.alert).modify(_ ++ b.alertsResponse.alert)

}

case class AlertsResponse(totalAlerts: Long, alert: List[Alert])

object AlertsResponse:
  given Codec[AlertsResponse] = CirceUtils.renamingCodec(deriveCodec[AlertsResponse], Map("alert" -> "Alert"))
