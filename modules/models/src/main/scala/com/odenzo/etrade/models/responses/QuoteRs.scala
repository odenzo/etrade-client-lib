package com.odenzo.etrade.models.responses

import cats.syntax.all.*
import cats.*
import cats.data.*
import cats.effect.IO
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.*
import io.circe.Decoder.{Result, decodeList}
import io.circe.generic.semiauto.*
import io.circe.Encoder.*
import io.circe.syntax.*

import java.time.Instant

/**
  * Well, this is an odd service. You can ask for different details. Depending on the SymbolType you still get different results. Best to
  * group like products in one call, e.g. all stocks.
  *   - FUNDAMENTAL: * STK => FUNDAMENTAL, * MF => MutualFund
  *   - INTRADAY
  *     1. STK => Intraday 2. MF => MutualFund
  *   - OPTIONS
  *     1. STK => Option (with bad data) 2. MF => MutualFund
  *   - WEEK52
  *     1. STK => Week52 2. MF => MutualFund
  *     - All
  *       1. STK => All 2. MF => MutualFund
  *
  * So, what a pain. We can try a hierarchy (sealed trait) and/or sniff on MutualFund, Intraday, Option, Fundamental, All. Thats probably
  * the best approach. Instead of built-in Circe discriminator I will write a custom sniffer on an anum of the case classes.
  *
  * Note: In practice I usually just get INTRADAY on STK and ALL on MutualFunds, sometimes All on Stocks too. ANd pick stuff out. If you
  * care about speed (over a web wervice!) Then just do one symbol type at a time and chnage the Model object to have only the fields you
  * care about (or JsonObject it and extract from there with pointer) Also has Option Messages on error?
  */
case class QuoteRs(messages: Option[Messages], quoteData: List[Quote]) // Array QuoteData and a messages:Messages
object QuoteRs {
  given codec: Codec.AsObject[QuoteRs] = CirceUtils.nestedCapitalizeCodec(deriveCodec[QuoteRs], "QuoteResponse")

}

/** This has quote type inside, ALL, MFund, etc... Parameterize or does all cover everything? HF */
case class Quote(
    dateTimeUTC: ETimestamp,
    quoteStatus: String, // e.g. CLOSING
    ahFlag: String,      // after hours flag, "true" , "false" :-( TODO Decoder this
    product: ETProduct,
    detail: QuoteDetails
)

object Quote:

  given encoder: Encoder[Quote] = Encoder
    .encodeJsonObject
    .contramap(_ => JsonObject.singleton("msg", Json.fromString("QuoteEncoder NotImplemented")))

  given decoder: Decoder[Quote] = Decoder[Quote] { hcursor =>

    val specificDetails: Result[QuoteDetails] = {
      hcursor
        .keys
        .fold(List.empty)(_.toList)
        .collectFirst[Result[QuoteDetails]] {
          case "MutualFund"  => hcursor.downField("MutualFund").as[MutualFundQuoteDetails]
          case "Intraday"    => hcursor.downField("Intraday").as[IntraDayQuoteDetails]
          case "Option"      => hcursor.downField("Option").as[OptionQuoteDetails]
          case "Week52"      => hcursor.downField("Week52").as[Week52QuoteDetails]
          case "Fundamental" => hcursor.downField("Fundamental").as[FundamentalQuoteDetails]
          case "All"         => hcursor.downField("All").as[AllQuoteDetails]
        }
        .getOrElse(DecodingFailure("No Matching Detail Type in Quote", null).asLeft)
    }

    for {
      details  <- specificDetails
      dateTime <- hcursor.downField("dateTimeUTC").as[ETimestamp]
      qstatus  <- hcursor.downField("quoteStatus").as[String]
      flag     <- hcursor.downField("ahFlag").as[String]
      product  <- hcursor.downField("Product").as[ETProduct] // Note: Capital P
    } yield Quote(dateTime, qstatus, flag, product, details)

  }

end Quote
