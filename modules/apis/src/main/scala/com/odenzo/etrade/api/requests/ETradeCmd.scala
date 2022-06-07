package com.odenzo.etrade.api.requests

import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.api.commands.*
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.responses.*
import com.odenzo.etrade.models.utils.*
import io.circe.{Codec, *}
import io.circe.Decoder.*
import io.circe.Encoder.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*

import java.time.LocalDate
import scala.annotation.unused
import scala.compiletime.constValue
import scala.deriving.Mirror
import scala.reflect.Typeable
import scala.util.chaining.*

/**
  * No strict reason to make this a sealed trait other than easy generation of matches etc. Each command just has to be a case class with a
  * Circe CODEC defined. Ones that are involved in paging often have a Semigroup or Monoid defined.
  *
  * Note: Using Circe 14.2 style derivation we: a) val a: ETradeCmd .asJson.as[ ETradeCmd] will work b) val aa: LookupProductCmd .asJson.as[
  * ETradeCmd] WILL NOT WORK. c)
  */
sealed trait ETradeCmd derives Codec.AsObject {
  type RESULT
}

case class ListAccountsCmd() extends ETradeCmd derives Codec.AsObject {
  override type RESULT = ListAccountsRs
}

//object ListAccountsCmd:
//  given codec: Codec.AsObject[ListAccountsCmd] = CirceUtils.deriveDiscCodec(this.toString)

/** This always does realTimeNav */

case class FetchAccountBalancesCmd(
    accountIdKey: String,
    accountType: Option[String] = None,
    instType: String = "BROKERAGE"
) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = AccountBalanceRs
}

//object AccountBalancesCmd:
//  given codec: Codec.AsObject[ListAccountsCmd] = CirceUtils.deriveDiscCodec(this.toString)

/** This will do paging automatically */
case class ListTransactionsCmd(
    accountIdKey: String,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    count: Int = 50
) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = ListTransactionsRs
}

//object ListTransactionsCmd:
//  given codec: Codec.AsObject[ListTransactionsCmd] = CirceUtils.deriveDiscCodec(this.toString)

case class FetchTxnDetailsCmd(
    accountIdKey: String,
    transactionId: String,
    storeId: Option[StoreId],
    txnType: Option[TransactionCategory]
) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = TransactionDetailsRs
}

//object TransactionDetailsCmd:
//  given codec: Codec.AsObject[TransactionDetailsCmd] = CirceUtils.deriveDiscCodec(this.toString)

case class ViewPortfolioCmd(
    accountIdKey: String,
    lots: Boolean = false,
    view: PortfolioView = PortfolioView.PERFORMANCE,
    totalsRequired: Boolean = true,
    marketSession: MarketSession,
    count: Int = 250
) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = ViewPortfolioRs
}

//object ViewPortfolioCmd:
//  given codec: Codec.AsObject[ViewPortfolioCmd] = CirceUtils.deriveDiscCodec(this.toString)

/** TODO: Change to one, varargs still to symbols (1+) Note that you cannot use MF_DETAIL on anything that is not a mutual fund. */
case class FetchQuoteCmd(
    symbols: NonEmptyChain[String],
    details: QuoteDetail = QuoteDetail.INTRADAY,
    requireEarnings: Boolean = false,
    skipMiniOptionsCheck: Boolean = true
) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = QuoteRs
}

//object EquityQuoteCmd:
// given codec: Codec.AsObject[EquityQuoteCmd] = CirceUtils.deriveDiscCodec(this.toString)

case class LookupProductCmd(searchFragment: String) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = LookupProductRs
}

/**
  * List the alerts for the user logged in, paged with 300 per page reduced.
  * @param catagory
  *   defaults to Stock and Account
  * @param status
  *   Defaults to READ and UNREAD
  * @param search
  *   Keyword to search the alert subject on.
  */
case class ListAlertsCmd(catagory: Option[AlertCategory], status: Option[AlertStatus], search: Option[String]) extends ETradeCmd
    derives Codec.AsObject {
  override type RESULT = ListAlertsRs
}

/**
  * Details about the given alert.
  * @param alertId
  */
case class ListAlertDetailsCmd(alertId: Long, htmlTags: Boolean = false) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = ListAlertDetailsRs
}

/**
  * Deletes all the listed alerts
  * @param alertIds
  */
case class DeleteAlertsCmd(alertIds: List[Long]) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = DeleteAlertsRs
}

case class GetOptionChainsCmd(
    symbol: String,
    expiryYear: Option[String],             //	query	no	Indicates the expiry year corresponding to which the optionchain
    // needs to be                             fetched
    expiryMonth: Option[String],            // query	no	Indicates the expiry month corresponding to which the optionchain needs to be fetched
    expiryDay: Option[String],              // query	no	Indicates the expiry day corresponding to which the optionchain needs to be fetched
    strikePriceNear: Option[Amount],        //	query	no	The optionchians fetched will have strike price nearer to this value
    noOfStrikes: Option[Int],               //	query	no	Indicates number of strikes for which the optionchain needs to be fetched
    includeWeekly: Option[Boolean],         //	query	no	The include weekly options request. Default: false.	true, false
    skipAdjusted: Option[Boolean],          // 	query	no	The skip adjusted request. Default: true.	true, false
    optionCategory: Option[OptionCategory], //	query	no	The option category. Default: STANDARD.	STANDARD, ALL, MINI
    chainType: Option[OptionsChainType],    //	query	no	The type of option chain. Default: CALLPUT.	CALL, PUT, CALLPUT
    priceType: Option[OptionsPriceType]     //	query	no	The price type. Default: ATNM.	ATNM, ALL
) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = GetOptionChainsRs
}

case class GetOptionExpiryCmd(symbol: String, expireType: Option[String]) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = GetOptionExpiryRs
}

/**
  * @param accountIdKey
  *   TODO: OT
  * @param dateRange
  *   case class
  * @param status
  *   // TODO: OrderStatus enum
  * @param symbols
  *   List of up to 25 symbols TODO: Opaque string type
  * @param securityType
  *   TODO: enum
  */
case class ListOrdersCmd(
    accountIdKey: String,
    dateRange: Option[(LocalDate, LocalDate)],
    status: Option[OrderStatus],
    symbols: Option[NonEmptyList[String]], // vararg this and internal NonEmptyList it?
    securityType: Option[String],
    txnType: Option[TransactionType],
    marketSession: Option[MarketSession],
    marker: Option[String] = None,
    count: Long = 0
) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = ListOrdersRs
}
case class PreviewOrderCmd(accountIdKey: String, previewOrderRequest: PreviewOrderRequest) extends ETradeCmd derives Codec.AsObject {
  override type RESULT = PreviewOrderRs
}
case class PlaceOrderCmd(accountIdKey: String, placeOrderRequest: PlaceOrderRequest)       extends ETradeCmd derives Codec.AsObject {
  override type RESULT = PlaceOrderRs
}
case class CancelOrderCmd(accountIdKey: String, cancelOrderRequest: CancelOrderRequest)    extends ETradeCmd derives Codec.AsObject {
  override type RESULT = CancelOrderRs
}
case class ChangePreviewedOrderCmd(accountIdKey: String, orderId: Long, previewOrderRequest: PreviewOrderRequest)
    extends ETradeCmd derives Codec.AsObject {
  override type RESULT = ChangePreviewOrderRs
}
case class PlaceChangedOrderCmd(accountIdKey: String, orderId: Long, placeOrderRequest: PlaceOrderRequest)
    extends ETradeCmd derives Codec.AsObject {
  override type RESULT = PlaceChangedOrderRs
}

//object LookupProductCmd:
// given codec: Codec.AsObject[LookupProductCmd] = CirceUtils.deriveDiscCodec(this.toString)

object ETradeCmd {
//  given dec: Decoder[ ETradeCmd] = Decoder[ ETradeCmd] { hc =>
//    // Unfortunately we cannot automatically generate the list?
//    hc.getOrElse[String](CirceUtils.discriminatorKey)("NO_DISCRIMINATOR")
//      .flatMap { discVal =>
//        val decoder: Decoder[ ETradeCmd] =
//          discVal match {
//            case "ListAccountsCmd"       => deriveDecoder[ListAccountsCmd].widen
//            case "AccountBalancesCmd"    => deriveDecoder[AccountBalancesCmd].widen
//            case "ListTransactionsCmd"   => deriveDecoder[ListTransactionsCmd].widen
//            case "TransactionDetailsCmd" => deriveDecoder[TransactionDetailsCmd].widen
//            case "ViewPortfolioCmd"      => deriveDecoder[ViewPortfolioCmd].widen
//            case "EquityQuoteCmd"        => deriveDecoder[EquityQuoteCmd].widen
//            case "LookupProductCmd"      => deriveDecoder[LookupProductCmd].widen
//            case "NO_DISCRIMINATOR"      => Decoder.failedWithMessage[ ETradeCmd](s"No Discriminator Found")
//            case other                   => Decoder.failedWithMessage[ ETradeCmd](s"Discriminator $other not mapped")
//          }
//        decoder(hc)
//      }
//  }
//
//  /** Encoding that delegates. Don't really have to, since I control the format. But in case some specific work needed. */
//  given enc: Encoder[ ETradeCmd] = Encoder
//    .AsObject
//    .instance { (cmd:  ETradeCmd) =>
//      cmd match
//        case a: ListAccountsCmd       => deriveEncoder[ListAccountsCmd].encodeObject(a)
//        case a: AccountBalancesCmd    => deriveEncoder[AccountBalancesCmd].encodeObject(a)
//        case a: ListTransactionsCmd   => deriveEncoder[ListTransactionsCmd].encodeObject(a)
//        case a: TransactionDetailsCmd => deriveEncoder[TransactionDetailsCmd].encodeObject(a)
//        case a: ViewPortfolioCmd      => deriveEncoder[ViewPortfolioCmd].encodeObject(a)
//        case a: EquityQuoteCmd        => deriveEncoder[EquityQuoteCmd].encodeObject(a)
//        case a: LookupProductCmd      => deriveEncoder[LookupProductCmd].encodeObject(a)
//    }
//
//  given Codec[ ETradeCmd] = Codec.from(dec, enc)

}
