package com.odenzo.etrade.api.commands

import cats.syntax.all.*
import cats.data.NonEmptyChain
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.responses.{
  AccountBalanceRs,
  ListAccountsRs,
  LookupProductRs,
  QuoteRs,
  TransactionDetailsRs,
  ListTransactionsRs,
  ViewPortfolioRs
}
import io.circe.*
import io.circe.Decoder.Result
import io.circe.syntax.*

import java.time.LocalDate
import io.circe.generic.semiauto.*

import scala.reflect.Typeable
import scala.util.chaining.*

sealed trait ETradeCmd {
  type RESULT
}

/**
  * Trying to build a command pattern such that a list of commands can return a typed list of responses using dependant function types. I am
  * not sure this is actually possible! In fact I think not with implicits. So, (1) Given Trait A, and A1, A2, A3 implementations If we have
  * a val x:A = A2 and summon a type class we should get a TC for A2, not a non-resolved. (2) Can we (aside from JSON) encode this as
  * Command[ResultType](name:String, x:Tuple(typed) or even just (name:String, TypedTuple()) => RESULTTYPE
  *
  * Conceptually a CommandPattern with ETradeCommand binding parameters (INP => T) in typeland, but not actually having the functionality.
  * Defining all the ETCmds in sealed trait. But we want different implementations to be bindable. In OOP land this would mean an abstract
  * function or subclassesing an implementation or actually binding to a ServiceInterface and supplying that.
  *
  * Well, I want to try binding to a TypeClass based on [INP,T]. This sketch works if input is A1 A2 typed, but not on A. Why, because there
  * is no "narrow" function. Could we define a TypeClass for [ETRADECMD] that has a match on a to get subclass and then return dependant
  * type? Then where would we bind the implementation function? We could delegate to subclass, e.g. case a:Foo => summon[Foo].exec() (Foo
  * .RESULTTYPE) being the result.But then all the match statement would return Object (or superclass of all the RESULTTYPE.
  *
  * So,flick, what if approach is Command[?] = Command[A1] etc. Where ? could be scoped to sealed trait or enum ADT Then the result is
  * Result[Any:Codec] for sure. Then, how can we bind a function? At that point we want a Command[X], Result[T] typeclessed summoned, with a
  * def execute(a:Command[X] (or X)) = Result[T]
  *
  * Note that Command[X] doesn't have to be a typeclass. If Command[X] always has a type RESULT_TYPE then we just need to summon[Command[X]]
  * and ensure its unique. Because Command[X] would/would really have tyoe Command[X & RESULT_TRAIT]
  */
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
            case "ListAccountsCmd"       => Decoder[ListAccountsCmd].widen
            case "AccountBalancesCmd"    => Decoder[AccountBalancesCmd].widen
            case "ListTransactionsCmd"   => Decoder[ListTransactionsCmd].widen
            case "TransactionDetailsCmd" => Decoder[TransactionDetailsCmd].widen
            case "ViewPortfolioCmd"      => Decoder[ViewPortfolioCmd].widen
            case "EquityQuoteCmd"        => Decoder[EquityQuoteCmd].widen
            case "LookupProductCmd"      => Decoder[LookupProductCmd].widen
            case "NO_DISCRIMINATOR"      => Decoder.failedWithMessage[ETradeCmd](s"No Discriminator Found")
            case other                   => Decoder.failedWithMessage[ETradeCmd](s"Discriminator $other not mapped")
          }
        decoder(hc)
      }
  }

  given enc: Encoder[ETradeCmd] = Encoder.AsObject { (cmd: ETradeCmd) =>
    scribe.info(s"Encoding ETradeCmd: $cmd")
    def command[T <: ETradeCmd](a: T): (Encoder.AsObject[T]) ?=> JsonObject = a.asJsonObject
    cmd match
      case a: ListAccountsCmd       => command(a)
      case a: AccountBalancesCmd    => command(a)
      case a: ListTransactionsCmd   => command(a)
      case a: TransactionDetailsCmd => command(a)
      case a: ViewPortfolioCmd      => command(a)
      case a: EquityQuoteCmd        => command(a)
      case a: LookupProductCmd      => command(a)
  }

  given Codec[ETradeCmd] = Codec.from(dec, enc)
}

def skolemEncode[T <: ETradeCmd](a: T): (enc: Encoder.AsObject[T]) ?=> JsonObject = a.asJsonObject

case class ListAccountsCmd() extends ETradeCmd {
  override type RESULT = ListAccountsRs
}

object ListAccountsCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[ListAccountsCmd]        = deriveEncoder[ListAccountsCmd].mapJsonObject(discriminator)
  given dec: Decoder[ListAccountsCmd]                 = deriveDecoder

/** This always does realTimeNav */

case class AccountBalancesCmd(
    accountIdKey: String,
    accountType: Option[String] = None,
    instType: String = "BROKERAGE"
) extends ETradeCmd {
  override type RESULT = AccountBalanceRs
}

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
) extends ETradeCmd {
  override type RESULT = ListTransactionsRs
}

object ListTransactionsCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[ListTransactionsCmd]    = deriveEncoder[ListTransactionsCmd].mapJsonObject(discriminator)
  given dec: Decoder[ListTransactionsCmd]             = deriveDecoder

case class TransactionDetailsCmd(
    accountIdKey: String,
    transactionId: String,
    storeId: Option[StoreId],
    txnType: Option[TransactionCategory]
) extends ETradeCmd {
  override type RESULT = TransactionDetailsRs
}

object TransactionDetailsCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  import com.odenzo.etrade.models.given
  given enc: Encoder.AsObject[TransactionDetailsCmd]  = deriveEncoder[TransactionDetailsCmd].mapJsonObject(discriminator)
  given dec: Decoder[TransactionDetailsCmd]           = deriveDecoder

case class ViewPortfolioCmd(
    accountIdKey: String,
    lots: Boolean = false,
    view: PortfolioView = PortfolioView.PERFORMANCE,
    totalsRequired: Boolean = true,
    marketSession: MarketSession,
    count: Int = 250
) extends ETradeCmd {
  override type RESULT = ViewPortfolioRs
}

object ViewPortfolioCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[ViewPortfolioCmd]       = deriveEncoder[ViewPortfolioCmd].mapJsonObject(discriminator)
  given dec: Decoder[ViewPortfolioCmd]                = deriveDecoder

/** TODO: Change to one, varargs still to symbols (1+) Note that you cannot use MF_DETAIL on anything that is not a mutual fund. */
case class EquityQuoteCmd(
    symbols: NonEmptyChain[String],
    details: QuoteDetail = QuoteDetail.INTRADAY,
    requireEarnings: Boolean = false,
    skipMiniOptionsCheck: Boolean = true
) extends ETradeCmd {
  override type RESULT = QuoteRs
}

object EquityQuoteCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[EquityQuoteCmd]         = deriveEncoder[EquityQuoteCmd].mapJsonObject(discriminator)
  given dec: Decoder[EquityQuoteCmd]                  = deriveDecoder[EquityQuoteCmd]

case class LookupProductCmd(searchFragment: String) extends ETradeCmd {
  override type RESULT = LookupProductRs
}

object LookupProductCmd:
  private val discriminator: JsonObject => JsonObject = ETradeCmd.postAddDiscriminator(this.toString)
  given enc: Encoder.AsObject[LookupProductCmd]       = deriveEncoder[LookupProductCmd].mapJsonObject(discriminator)
  given dec: Decoder[LookupProductCmd]                = deriveDecoder[LookupProductCmd]
