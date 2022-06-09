package com.odenzo.etradeapp.purebackend

import io.circe.*
import io.circe.syntax.given
import com.odenzo.etrade.api.requests.PreviewOrderCmd
import cats.data.*
import cats.effect.IO
import cats.syntax.all.{*, given}
import com.odenzo.etrade.api.*
import com.odenzo.etrade.api.requests.*
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.orderwriting.*
import org.http4s.client.Client

import java.time.Instant
import java.util.UUID

/** Orders are complicated. Here is a helpers collection. */
class OrdersUsage(val account: Account)(using Client[IO], ETradeContext) {
  import com.odenzo.etrade.api.commands.{given, *}
  def exercise(): IO[Unit] = previewAnOrder()

  /** Buy Equity on Market with given Limit, e.g. GOOG 500.00 */
  val instrument: InstrumentForOrder = InstrumentForOrder(
    product = ETProduct("GOOG", SecutiyType.EQ),
    orderAction = OrderAction.BUY,
    quantityType = OrderQuantityType.DOLLAR,
    quantity = BigDecimal("2400.00")
  )

  val orderDetail: DetailForEquityOrder = DetailForEquityOrder(
    accountId = account.accountId,
    priceType = OrderPricingType.LIMIT,
    marketSession = MarketSession.REGULAR,
    orderType = OrderType.EQ,
    orderTerm = OrderTerm.GOOD_FOR_DAY,
    limitPrice = Option(BigDecimal("180.00")),
    allOrNone = false,
    instrument = List(instrument)
  )

  def genCLientOrderId           = UUID.randomUUID().toString.filter(_ != '-').take(19)
  def previewAnOrder(): IO[Unit] = {

    for {
      _         <- IO(scribe.info(s"Using Account: ${account.asJson.spaces4}"))
      previewRq  = PreviewOrderCmd(
                     account.accountIdKey,
                     PreviewOrderRequest(OrderType.EQ, List(orderDetail), clientOrderId = genCLientOrderId)
                   )
      previewRs <- previewRq.exec()
    } yield ()

  }
}

/*
 <PlaceOrderRequest>
//    <orderType>EQ</orderType>
//    <clientOrderId>sd464333</clientOrderId>
//    <PreviewIds>
//       <previewId>730206520</previewId>
//    </PreviewIds>
//    <Order>
//       <allOrNone>false</allOrNone>
//       <priceType>LIMIT</priceType>
//       <orderTerm>GOOD_FOR_DAY</orderTerm>
//       <marketSession>REGULAR</marketSession>
//       <stopPrice />
//       <limitPrice>188.51</limitPrice>
//       <Instrument>
//          <Product>
//             <securityType>EQ</securityType>
//             <symbol>FB</symbol>
//          </Product>
//          <orderAction>BUY</orderAction>
//          <quantityType>QUANTITY</quantityType>
//          <quantity>150</quantity>
//       </Instrument>
//    </Order>
 */
/*
$request_params = new Nothing
($request_params) => __set('accountId', 83405188)// sample values

($request_params) => __set('clientOrderId', 'asdf1234')
($request_params) => __set('limitPrice', 300)
($request_params) => __set('previewId', '')
($request_params) => __set('stopPrice', 300)
($request_params) => __set('allOrNone', '')
($request_params) => __set('quantity', 4)
($request_params) => __set('reserveOrder', '')
($request_params) => __set('reserveQuantity', 0)
($request_params) => __set('stopLimitPrice', '')
($request_params) => __set('symbol', 'AAPL')
($request_params) => __set('orderAction', 'BUY')
($request_params) => __set('priceType', 'LIMIT')
($request_params) => __set('routingDestination', '')
($request_params) => __set('marketSession', 'REGULAR')
($request_params) => __set('orderTerm', 'GOOD_FOR_DAY')
 */
