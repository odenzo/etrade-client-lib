package com.odenzo.etrade.api.requests

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.api.*
import com.odenzo.etrade.models.{given, *}
import com.odenzo.etrade.models.responses.{LookupProductRs, QuoteRs}
import org.http4s.Method.{DELETE, GET, POST, PUT}
import org.http4s.Request
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
object OrdersApi extends APIHelper {

  def listOrdersCF(cmd: ListOrdersCmd): ETradeCall = IO.pure {

    val csvMaybe = cmd.symbols.map(_.take(25).mkString(","))
    Request[IO](
      GET,
      (baseUri / "v1" / "accounts" / cmd.accountIdKey / "orders")
        .withOptionQueryParam("marker", cmd.marker)
        .withOptionQueryParam("symbol", csvMaybe)
        .withOptionQueryParam("fromDate", cmd.dateRange.map(_._1.toString))
        .withOptionQueryParam("toDate", cmd.dateRange.map(_._2.toString))
        .withOptionQueryParam("securityType", cmd.securityType.map(_.toString))
        .withOptionQueryParam("transactionType", cmd.txnType.map(_.toString))
        .withOptionQueryParam("marketSession", cmd.marketSession.map(_.toString)),
      headers = acceptJsonHeaders
    )
  }

  def listOrdersApp(cmd: ListOrdersCmd): ETradeService[cmd.RESULT] = {
    val rqIO = listOrdersCF(cmd)
    standard[cmd.RESULT](rqIO)
  }

  def previewOrderCF(cmd: PreviewOrderCmd): ETradeCall = IO.pure {
    Request[IO](
      POST,
      (baseUri / "v1" / "accounts" / cmd.accountIdKey / "orders" / "preview"),
      headers = acceptJsonHeaders
    ).withEntity(cmd.previewOrderRequest)
  }

  def previewOrderApp(cmd: PreviewOrderCmd): ETradeService[cmd.RESULT] = {
    val rqIO = previewOrderCF(cmd)
    standard[cmd.RESULT](rqIO)
  }

  def placeOrderCF(cmd: PlaceOrderCmd): ETradeCall                 = IO.pure {
    Request[IO](
      POST,
      (baseUri / "v1" / "accounts" / cmd.accountIdKey / "orders" / "place"),
      headers = acceptJsonHeaders
    ).withEntity(cmd.placeOrderRequest)
  }
  def placeOrderApp(cmd: PlaceOrderCmd): ETradeService[cmd.RESULT] = {
    val rqIO = placeOrderCF(cmd)
    standard[cmd.RESULT](rqIO)
  }

  def cancelOrderCF(cmd: CancelOrderCmd): ETradeCall                 = IO.pure {
    Request[IO](
      PUT,
      (baseUri / "v1" / "accounts" / cmd.accountIdKey / "orders" / "cancel"),
      headers = acceptJsonHeaders
    ).withEntity(cmd.cancelOrderRequest)
  }
  def cancelOrderApp(cmd: CancelOrderCmd): ETradeService[cmd.RESULT] = {
    val rqIO: ETradeCall = cancelOrderCF(cmd)
    standard[cmd.RESULT](rqIO)
  }

  def changePreviewedOrderCF(cmd: ChangePreviewedOrderCmd): ETradeCall                 = IO.pure {
    Request[IO](
      PUT,
      (baseUri / "v1" / "accounts" / cmd.accountIdKey / "orders" / cmd.orderId / "change" / "preview"),
      headers = acceptJsonHeaders
    ).withEntity(cmd.previewOrderRequest)
  }
  def changePreviewedOrderApp(cmd: ChangePreviewedOrderCmd): ETradeService[cmd.RESULT] = {
    val rqIO: ETradeCall = changePreviewedOrderCF(cmd)
    standard[cmd.RESULT](rqIO)
  }

  def placeChangedOrderCF(cmd: PlaceChangedOrderCmd): ETradeCall                 = IO.pure {
    Request[IO](
      PUT,
      (baseUri / "v1" / "accounts" / cmd.accountIdKey / "orders" / cmd.orderId / "change" / "preview"),
      headers = acceptJsonHeaders
    ).withEntity(cmd.placeOrderRequest)
  }
  def placeChangedOrderApp(cmd: PlaceChangedOrderCmd): ETradeService[cmd.RESULT] = {
    val rqIO = placeChangedOrderCF(cmd)
    standard[cmd.RESULT](rqIO)
  }

}
