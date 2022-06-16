package com.odenzo.etrade.api.requests.experiment

import cats.data.NonEmptyChain
import cats.syntax.all.*

import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.responses.*
import com.odenzo.etrade.models.utils.*
import io.circe.*
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
  * Note: Using Circe 14.2 style derivation we: a) val a:ETradeCmdV2.asJson.as[ETradeCmd] will work b) val aa: LookupProductCmd
  * .asJson.as[ETradeCmd] WILL NOT WORK. c)
  */
sealed trait ETradeCmdV2 {
  type RESULT
}

// This gets an error if an emptytuple
case class ListAccountsCmdV2() extends ETradeCmdV2 {
  override type RESULT = ListAccountsRs
}

object ListAccountsCmdV2:
  given codec: Codec.AsObject[ListAccountsCmdV2] = CirceCodecs.deriveDiscCodec()

/** This always does realTimeNav */

case class AccountBalancesCmdV2(
    accountIdKey: String,
    accountType: Option[String] = None,
    instType: String = "BROKERAGE"
) extends ETradeCmdV2 {
  override type RESULT = AccountBalanceRs
}

object AccountBalancesCmdV2:
  given codec: Codec.AsObject[AccountBalancesCmdV2] = CirceCodecs.deriveDiscCodec()

/** This will do paging automatically */
case class ListTransactionsCmdV2(
    accountIdKey: String,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    count: Int = 50
) extends ETradeCmdV2 {
  override type RESULT = ListTransactionsRs
}

object ListTransactionsCmdV2:
  given codec: Codec.AsObject[ListTransactionsCmdV2] = CirceCodecs.deriveDiscCodec()

case class TransactionDetailsCmdV2(
    accountIdKey: String,
    transactionId: String,
    storeId: Option[StoreId],
    txnType: Option[TransactionCategory]
) extends ETradeCmdV2 {
  override type RESULT = TransactionDetailsRs
}

object TransactionDetailsCmdV2:
  given codec: Codec.AsObject[TransactionDetailsCmdV2] = CirceCodecs.deriveDiscCodec()

case class ViewPortfolioCmdV2(
    accountIdKey: String,
    lots: Boolean = false,
    view: PortfolioView = PortfolioView.PERFORMANCE,
    totalsRequired: Boolean = true,
    marketSession: MarketSession,
    count: Int = 250
) extends ETradeCmdV2 {
  override type RESULT = ViewPortfolioRs
}

object ViewPortfolioCmdV2:
  given codec: Codec.AsObject[ViewPortfolioCmdV2] = CirceCodecs.deriveDiscCodec()

/** TODO: Change to one, varargs still to symbols (1+) Note that you cannot use MF_DETAIL on anything that is not a mutual fund. */
case class EquityQuoteCmdV2(
    symbols: NonEmptyChain[String],
    details: QuoteDetail = QuoteDetail.INTRADAY,
    requireEarnings: Boolean = false,
    skipMiniOptionsCheck: Boolean = true
) extends ETradeCmdV2 {
  override type RESULT = QuoteRs
}

object EquityQuoteCmdV2:
  given codec: Codec.AsObject[EquityQuoteCmdV2] = CirceCodecs.deriveDiscCodec()

case class LookupProductCmdV2(searchFragment: String) extends ETradeCmdV2 {
  override type RESULT = LookupProductRs
}

object LookupProductCmdV2:
  given codec: Codec.AsObject[LookupProductCmdV2] = CirceCodecs.deriveDiscCodec()

object ETradeCmdV2 {
  given dec: Decoder[ETradeCmdV2] = Decoder[ETradeCmdV2] { hc =>
    // Unfortunately we cannot automatically generate the list?
    hc.getOrElse[String](CirceCodecs.DiscCodec.discriminatorKey)("NO_DISCRIMINATOR")
      .flatMap { discVal =>
        val decoder: Decoder[ETradeCmdV2] =
          discVal match {
            case "ListAccountsCmdV2"       => deriveDecoder[ListAccountsCmdV2].widen
            case "AccountBalancesCmdV2"    => deriveDecoder[AccountBalancesCmdV2].widen
            case "ListTransactionsCmdV2"   => deriveDecoder[ListTransactionsCmdV2].widen
            case "TransactionDetailsCmdV2" => deriveDecoder[TransactionDetailsCmdV2].widen
            case "ViewPortfolioCmdV2"      => deriveDecoder[ViewPortfolioCmdV2].widen
            case "EquityQuoteCmdV2"        => deriveDecoder[EquityQuoteCmdV2].widen
            case "LookupProductCmdV2"      => deriveDecoder[LookupProductCmdV2].widen
            case "NO_DISCRIMINATOR"        => Decoder.failedWithMessage[ETradeCmdV2](s"No Discriminator Found")
            case other                     => Decoder.failedWithMessage[ETradeCmdV2](s"Discriminator $other not mapped")
          }
        decoder(hc)
      }
  }

  /**
    * Encoding that delegates. Don't really have to, since I control the format. But in case some specific work needed. This is really just
    * a narrowing then delegating to the encoder of narrowed type. Typeable help here? This definately should be doable in a macro maybe
    * inline def simply. First get the discriminator automatically created.
    */
  given enc: Encoder.AsObject[ETradeCmdV2] = Encoder
    .AsObject
    .instance { (cmd: ETradeCmdV2) =>
      scribe.info(s"V2Trait Encoding $cmd")

      cmd match
        case a: ListAccountsCmdV2       => a.asJsonObject
        case a: AccountBalancesCmdV2    => a.asJsonObject
        case a: ListTransactionsCmdV2   => a.asJsonObject
        case a: TransactionDetailsCmdV2 => a.asJsonObject
        case a: ViewPortfolioCmdV2      => a.asJsonObject
        case a: EquityQuoteCmdV2        => a.asJsonObject
        case a: LookupProductCmdV2      => a.asJsonObject
    }

  // given Codec[ETradeCmdV2] = Codec.from(dec, enc)

}
