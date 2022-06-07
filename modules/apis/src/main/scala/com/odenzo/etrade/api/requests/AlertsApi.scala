package com.odenzo.etrade.api.requests

import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import cats.effect.syntax.all.*
import com.odenzo.etrade.models.*
import org.http4s.Method.{DELETE, GET}
import org.http4s.Request
import com.odenzo.etrade.api.*
import com.odenzo.etrade.models.responses.{LookupProductRs, QuoteRs}

object AlertsApi extends APIHelper {

  def listAlertsCF(category: Option[AlertCategory], status: Option[AlertStatus], search: Option[String]): ETradeCall = IO.pure {
    Request[IO](
      GET,
      (baseUri / "v1" / "user" / "alerts")
        .withOptionQueryParam("category", category.map(_.toString))
        .withOptionQueryParam("status", status.map(_.toString))
        .withOptionQueryParam("search", search),
      headers = acceptJsonHeaders
    )
  }

  def listAlertsApp(cmd: ListAlertsCmd): ETradeService[cmd.RESULT] = {
    val rqIO = listAlertsCF.tupled(Tuple.fromProductTyped(cmd))
    standard[cmd.RESULT](rqIO)
  }

  def listAlertDetailsCF(id: Long, htmlTags: Boolean): ETradeCall = IO.pure(Request[IO](
    GET,
    (baseUri / "v1" / "user" / "alerts" / id), // .withQueryParam("htmlTags", htmlTags)
    headers = acceptJsonHeaders
  ))

  def listAlertDetailsApp(cmd: ListAlertDetailsCmd): ETradeService[cmd.RESULT] = {
    val rqIO = listAlertDetailsCF.tupled(Tuple.fromProductTyped(cmd))
    standard[cmd.RESULT](rqIO)
  }

  def deleteAlertsCF(cmd: DeleteAlertsCmd): ETradeCall                = IO.pure {
    val csv = cmd.alertIds.mkString(",")
    Request[IO](
      DELETE,
      (baseUri / "v1" / "user" / "alerts" / csv),
      headers = acceptJsonHeaders
    )
  }
  def deleteAlertApp(cmd: DeleteAlertsCmd): ETradeService[cmd.RESULT] = {
    val rqIO = deleteAlertsCF(cmd)
    standard[cmd.RESULT](rqIO)
  }

}
