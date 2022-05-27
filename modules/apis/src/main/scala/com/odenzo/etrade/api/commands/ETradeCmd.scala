package com.odenzo.etrade.api.commands

import cats.syntax.all.*
import cats.data.NonEmptyChain
import com.odenzo.etrade.models.{MarketSession, PortfolioView, QuoteDetail, QuoteDetails, TransactionCategory}
import io.circe.*
import io.circe.Decoder.Result
import io.circe.syntax.*

import java.time.LocalDate
import io.circe.generic.semiauto.*

import scala.util.chaining.*
sealed trait ETradeCmd

object ETradeCmd {
  final val discriminatorKey                                            = "_I_AM_"
  def postAddDiscriminator(myName: String)(obj: JsonObject): JsonObject = {
    obj.add(discriminatorKey, Json.fromString(myName))
  }

  def withDiscriminator[T: Encoder.AsObject](t: T): JsonObject = Encoder
    .AsObject[T]
    .encodeObject(t)
    .pipe(postAddDiscriminator(t.getClass.getSimpleName))

  given dec: Decoder[ETradeCmd] = Decoder[ETradeCmd] { hc =>
    // Unfortunately we cannot automatically generate the list?
    hc.getOrElse[String](discriminatorKey)("NO_DISCRIMINATOR")
      .flatMap { discVal =>
        val decoder: Decoder[ETradeCmd] =
          discVal match {
            case "ListAccounts"       => Decoder[ListAccountsCmd].widen
            case "AccountBalances"    => Decoder[AccountBalancesCmd].widen
            case "ListTransactions"   => Decoder[ListTransactionsCmd].widen
            case "TransactionDetails" => Decoder[TransactionDetailsCmd].widen
            case "ViewPortfolio"      => Decoder[ViewPortfolioCmd].widen
            case "EquityQuote"        => Decoder[EquityQuoteCmd].widen
            case "LookupProduct"      => Decoder[LookupProductCmd].widen
            case "NO_DISCRIMINATOR"   => Decoder.failedWithMessage[ETradeCmd](s"No Discriminator Found")
            case other                => Decoder.failedWithMessage[ETradeCmd](s"Discriminator $other not mapped")
          }
        decoder(hc)
      }
  }

  val enc: Encoder[ETradeCmd] = Encoder.AsObject { (cmd: ETradeCmd) =>
    scribe.info(s"Encoding ETradeCmd: $cmd")
    cmd match
      case c: ListAccountsCmd       => Encoder.AsObject[ListAccountsCmd].encodeObject(c)
      case c: AccountBalancesCmd    => Encoder.AsObject[AccountBalancesCmd].encodeObject(c)
      case c: ListTransactionsCmd   => Encoder.AsObject[ListTransactionsCmd].encodeObject(c)
      case c: TransactionDetailsCmd => Encoder.AsObject[TransactionDetailsCmd].encodeObject(c)
      case c: ViewPortfolioCmd      => Encoder.AsObject[ViewPortfolioCmd].encodeObject(c)
      case c: EquityQuoteCmd        => Encoder.AsObject[EquityQuoteCmd].encodeObject(c)
      case c: LookupProductCmd      => Encoder.AsObject[LookupProductCmd].encodeObject(c)
  }

  given Codec[ETradeCmd] = Codec.from(dec, enc)
}

case class ListAccountsCmd() extends ETradeCmd

object ListAccountsCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[ListAccountsCmd]        = deriveEncoder[ListAccountsCmd].mapJsonObject(discriminator)
  given dec: Decoder[ListAccountsCmd]                 = deriveDecoder

case class AccountBalancesCmd(
    accountIdKey: String,
    accountType: Option[String] = None,
    instType: String = "BROKERAGE",
    realTimeNav: Boolean = true
) extends ETradeCmd

object AccountBalancesCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[AccountBalancesCmd]     = deriveEncoder[AccountBalancesCmd].mapJsonObject(discriminator)
  given dec: Decoder[AccountBalancesCmd]              = deriveDecoder

/** This will do paging automatically */
case class ListTransactionsCmd(
    accountIdKey: String,
    startDate: Option[LocalDate] = None,
    endDate: Option[LocalDate] = None,
    count: Int = 50
) extends ETradeCmd

object ListTransactionsCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[ListTransactionsCmd]    = deriveEncoder[ListTransactionsCmd].mapJsonObject(discriminator)
  given dec: Decoder[ListTransactionsCmd]             = deriveDecoder

case class TransactionDetailsCmd(accountIdKey: String, transactionId: String, storeId: Option[String], txnType: Option[TransactionCategory])
    extends ETradeCmd

object TransactionDetailsCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[TransactionDetailsCmd]  = deriveEncoder[TransactionDetailsCmd].mapJsonObject(discriminator)
  given dec: Decoder[TransactionDetailsCmd]           = deriveDecoder

case class ViewPortfolioCmd(
    accountIdKey: String,
    lots: Boolean = false,
    view: PortfolioView = PortfolioView.PERFORMANCE,
    totalsRequired: Boolean = true,
    marketSession: MarketSession,
    count: Int = 250
) extends ETradeCmd

object ViewPortfolioCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[ViewPortfolioCmd]       = deriveEncoder[ViewPortfolioCmd].mapJsonObject(discriminator)
  given dec: Decoder[ViewPortfolioCmd]                = deriveDecoder

case class EquityQuoteCmd(symbols: NonEmptyChain[String], details: QuoteDetail = QuoteDetail.INTRADAY, requireEarnings: Boolean = false)
    extends ETradeCmd

object EquityQuoteCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[EquityQuoteCmd]         = deriveEncoder[EquityQuoteCmd].mapJsonObject(discriminator)
  given dec: Decoder[EquityQuoteCmd]                  = deriveDecoder[EquityQuoteCmd]

case class LookupProductCmd(searchFragment: String) extends ETradeCmd

object LookupProductCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[LookupProductCmd]       = deriveEncoder[LookupProductCmd].mapJsonObject(discriminator)
  given dec: Decoder[LookupProductCmd]                = deriveDecoder[LookupProductCmd]
