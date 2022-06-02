package com.odenzo.etrade.api.requests

import cats.data.NonEmptyChain
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

case class AccountBalancesCmd(
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

case class TransactionDetailsCmd(
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
case class EquityQuoteCmd(
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
