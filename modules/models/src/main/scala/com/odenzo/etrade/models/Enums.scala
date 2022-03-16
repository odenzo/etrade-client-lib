package com.odenzo.etrade.models

import cats.data.Chain
import com.odenzo.etrade.base.CirceCodecs.*
import io.circe.*
import io.circe.generic.semiauto.deriveCodec
import io.circe.Decoder.*
import io.circe.Encoder.*
import io.circe.Codec.*

import scala.util.Try

/** MF_DETAIL can only be used on valid mutual funds apparently */
enum QuoteDetail:
  case ALL, FUNDAMENTAL, INTRADAY, OPTIONS, WEEK_52, MF_DETAIL

object QuoteDetail:
  given Codec[QuoteDetail] = Codec.from(
    stringCIEnumDecoder[QuoteDetail],
    stringCIEnumEncoder[QuoteDetail]
  )

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
