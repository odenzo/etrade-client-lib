package com.odenzo.etrade.models

import cats.data.Chain
import com.odenzo.etrade.models.utils.CirceCodecs.*
import io.circe.*
import io.circe.generic.semiauto.deriveCodec
import io.circe.Decoder.*
import io.circe.Encoder.*
import io.circe.Codec.*

import scala.util.Try

enum ETResult:
  case SUCCESS, ERROR

object ETResult:
  given Codec[ETResult] = Codec.from(stringCIEnumDecoder[ETResult], stringCIEnumEncoder[ETResult])

/** MF_DETAIL can only be used on valid mutual funds apparently */
enum QuoteDetail:
  case ALL, FUNDAMENTAL, INTRADAY, OPTIONS, WEEK_52, MF_DETAIL

object QuoteDetail:
  given Codec[QuoteDetail] = Codec.from(stringCIEnumDecoder[QuoteDetail], stringCIEnumEncoder[QuoteDetail])

enum PortfolioView:
  case PERFORMANCE, FUNDAMENTAL, OPTIONSWATCH, QUICK, COMPLETE

object PortfolioView:
  given Codec[PortfolioView] = Codec.from(
    stringCIEnumDecoder[PortfolioView],
    stringCIEnumEncoder[PortfolioView]
  )
enum QuoteStatus:
  case REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED

object QuoteStatus:
  given Codec[QuoteStatus] = Codec.from(
    stringCIEnumDecoder[QuoteStatus],
    stringCIEnumEncoder[QuoteStatus]
  )

enum MarketSession:
  case REGULAR, EXTENDED

object MarketSession:
  given Codec[MarketSession] = Codec.from(
    stringCIEnumDecoder[MarketSession],
    stringCIEnumEncoder[MarketSession]
  )

enum OptionsChainType:
  case CALL, PUT, CALLPUT

object OptionsChainType:
  given Codec[OptionsChainType] = Codec.from(
    stringCIEnumDecoder[OptionsChainType],
    stringCIEnumEncoder[OptionsChainType]
  )

enum OptionsPriceType:
  case ATNM, ALL

object OptionsPriceType:
  given Codec[OptionsPriceType] = Codec.from(stringCIEnumDecoder[OptionsPriceType], stringCIEnumEncoder[OptionsPriceType])

enum OptionCategory:
  case STANDARD, ALL, MINI

object OptionCategory:
  given Codec[OptionCategory] = Codec.from(
    stringCIEnumDecoder[OptionCategory],
    stringCIEnumEncoder[OptionCategory]
  )

enum TransactionType:
  case ATNM, BUY, SELL, SELL_SHORT, BUY_TO_COVER, MF_EXCHANGE

object TransactionType:
  given Codec[TransactionType] = Codec.from(stringCIEnumDecoder[TransactionType], stringCIEnumEncoder[TransactionType])

enum AlertStatus:
  case READ, UNREAD, DELETED, UNDELETED

object AlertStatus:
  given Codec[AlertStatus] = Codec.from(
    stringCIEnumDecoder[AlertStatus],
    stringCIEnumEncoder[AlertStatus]
  )

enum AlertCategory:
  case READ, UNREAD, DELETED

object AlertCategory:
  given Codec[AlertCategory] = Codec.from(stringCIEnumDecoder[AlertCategory], stringCIEnumEncoder[AlertCategory])

enum CashMargin:
  case CASH, MARGIN

object CashMargin:
  given Codec[CashMargin] = Codec.from(stringCIEnumDecoder[CashMargin], stringCIEnumEncoder[CashMargin])

enum OrderStatus:
  case OPEN, EXECUTED, CANCELLED, INDIVIDUAL_FILLS, CANCEL_REQUESTED, EXPIRED, REJECTED

object OrderStatus:
  given Codec[OrderStatus] = Codec.from(stringCIEnumDecoder[OrderStatus], stringCIEnumEncoder[OrderStatus])

enum OrderType:
  case EQ, OPTN, SPREADS, BUY_WRITES, BUTTERFLY, IRON_BUTTERFLY, CONDOR, IRON_CONDOR, MF, MMF
object OrderType:
  given Codec[OrderType] = Codec.from(stringCIEnumDecoder[OrderType], stringCIEnumEncoder[OrderType])

/** How long the order stays in effect */
enum OrderTerm:
  case GOOD_UNTIL_CANCEL, GOOD_FOR_DAY, GOOD_TILL_DATE, IMMEDIATE_OR_CANCEL, FILL_OR_KILL

object OrderTerm:
  given Codec[OrderTerm] = Codec.from(stringCIEnumDecoder[OrderTerm], stringCIEnumEncoder[OrderTerm])

enum OrderPricingType:
  case MARKET, LIMIT, STOP, STOP_LIMIT, TRAILING_STOP_CNST_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_TRAILING_STOP_CNST,
    TRAILING_STOP_PRCT_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_TRAILING_STOP_PRCT, TRAILING_STOP_CNST, TRAILING_STOP_PRCT, HIDDEN_STOP,
    HIDDEN_STOP_BY_LOWER_TRIGGER, UPPER_TRIGGER_BY_HIDDEN_STOP, NET_DEBIT, NET_CREDIT, NET_EVEN, MARKET_ON_OPEN, MARKET_ON_CLOSE,
    LIMIT_ON_OPEN, LIMIT_ON_CLOSE

object OrderPricingType:
  given Codec[OrderPricingType] = Codec.from(stringCIEnumDecoder[OrderPricingType], stringCIEnumEncoder[OrderPricingType])

enum OrderOffsetType:
  case TRAILING_STOP_CNST, TRAILING_STOP_PRCT

object OrderOffsetType:
  given Codec[OrderOffsetType] = Codec.from(stringCIEnumDecoder[OrderOffsetType], stringCIEnumEncoder[OrderOffsetType])

enum OrderRouting:
  case AUTO, AMEX, BOX, CBOE, ISE, NOM, NYSE, PHX

object OrderRouting:
  given Codec[OrderRouting] = Codec.from(stringCIEnumDecoder[OrderRouting], stringCIEnumEncoder[OrderRouting])

enum OrderConditionType:
  case CONTINGENT_GTE, CONTINGENT_LTE

object OrderConditionType:
  given Codec[OrderConditionType] = Codec.from(stringCIEnumDecoder[OrderConditionType], stringCIEnumEncoder[OrderConditionType])

enum OrderFollowPriceType:
  case ASK, BID, LAST

object OrderFollowPriceType:
  given Codec[OrderFollowPriceType] = Codec.from(stringCIEnumDecoder[OrderFollowPriceType], stringCIEnumEncoder[OrderFollowPriceType])

enum PositionType:
  case ENTIRE_POSITION, CASH, MARGIN

object PositionType:
  given Codec[PositionType] = Codec.from(stringCIEnumDecoder[PositionType], stringCIEnumEncoder[PositionType])

enum ExecutionGuarantee:
  case EG_QUAL_UNSPECIFIED, EG_QUAL_QUALIFIED, EG_QUAL_NOT_IN_FORCE, EG_QUAL_NOT_A_MARKET_ORDER, EG_QUAL_NOT_AN_ELIGIBLE_SECURITY,
    EG_QUAL_INVALID_ORDER_TYPE, EG_QUAL_SIZE_NOT_QUALIFIED, EG_QUAL_OUTSIDE_GUARANTEED_PERIOD, EG_QUAL_INELIGIBLE_GATEWAY,
    EG_QUAL_INELIGIBLE_DUE_TO_IPO, EG_QUAL_INELIGIBLE_DUE_TO_SELF_DIRECTED, EG_QUAL_INELIGIBLE_DUE_TO_CHANGEORDER

object ExecutionGuarantee:
  given Codec[ExecutionGuarantee] = Codec.from(stringCIEnumDecoder[ExecutionGuarantee], stringCIEnumEncoder[ExecutionGuarantee])

enum ReinvestOption:
  case REINVEST, DEPOSIT, CURRENT_HOLDING

object ReinvestOption:
  given Codec[ReinvestOption] = Codec.from(stringCIEnumDecoder[ReinvestOption], stringCIEnumEncoder[ReinvestOption])

enum OrderEventType:
  case UNSPECIFIED, ORDER_PLACED, SENT_TO_CMS, SENT_TO_MARKET, MARKET_SENT_ACKNOWLEDGED, CANCEL_REQUESTED, ORDER_MODIFIED,
    ORDER_SENT_TO_BROKER_REVIEW, SYSTEM_REJECTED, ORDER_REJECTED, ORDER_CANCELLED, CANCEL_REJECTED, ORDER_EXPIRED, ORDER_EXECUTED,
    ORDER_ADJUSTED, ORDER_REVERSED, REVERSE_CANCELLATION, REVERSE_EXPIRATION, OPTION_POSITION_ASSIGNED, OPEN_ORDER_ADJUSTED, CA_CANCELLED,
    CA_BOOKED, IPO_ALLOCATED, DONE_TRADE_EXECUTED, REJECTION_REVERSAL

object OrderEventType:
  given Codec[OrderEventType] = Codec.from(stringCIEnumDecoder[OrderEventType], stringCIEnumEncoder[OrderEventType])

enum OrderAction:
  case BUY, SELL, BUY_TO_COVER, SELL_SHORT, BUY_OPEN, BUY_CLOSE, SELL_OPEN, SELL_CLOSE, EXCHANGE

object OrderAction:
  given Codec[OrderAction] = Codec.from(stringCIEnumDecoder[OrderAction], stringCIEnumEncoder[OrderAction])

enum OrderQuantityType:
  case QUANTITY, DOLLAR, ALL_I_OWN

object OrderQuantityType:
  given Codec[OrderQuantityType] = Codec.from(stringCIEnumDecoder[OrderQuantityType], stringCIEnumEncoder[OrderQuantityType])

enum MFTransaction:
  case BUY, SELL

object MFTransaction:
  given Codec[MFTransaction] = Codec.from(stringCIEnumDecoder[MFTransaction], stringCIEnumEncoder[MFTransaction])

enum OptionExpireType: // TODO: Accition ExpireTypes
  case EQUITY

object OptionExpireType:
  given Codec[OptionExpireType] = Codec.from(stringCIEnumDecoder[OptionExpireType], stringCIEnumEncoder[OptionExpireType])

enum MarginLevel:
  case UNSPECIFIED, MARGIN_TRADING_NOT_ALLOWED,
    MARGIN_TRADING_ALLOWED, MARGIN_TRADING_ALLOWED_ON_OPTIONS, MARGIN_TRADING_ALLOWED_ON_PM

object MarginLevel:
  given Codec[MarginLevel] = Codec.from(stringCIEnumDecoder[MarginLevel], stringCIEnumEncoder[MarginLevel])
