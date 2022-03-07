package com.odenzo.etrade.models.responses

import cats.syntax.all.*
import cats.data.Chain
import cats.effect.IO
import com.odenzo.base.CirceUtils
import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.ComputedBalance.rename
import io.circe.*
import io.circe.Decoder.{Result, decodeList}
import io.circe.generic.semiauto.*
import io.circe.Encoder.*
import io.circe.syntax.*

import java.time.Instant

/**
  * Well, this is an odd service. You can ask for different details. Depending on the SymbolType you still get different results.
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
case class QuoteRs(quotes: List[JsonObject])
object QuoteRs {
  given decoder: Decoder[QuoteRs] = {
    val baseDecoder: Decoder[List[JsonObject]] = decodeList[JsonObject]
    val nestDecoder: Decoder[List[JsonObject]] = baseDecoder.prepare(ac => ac.downField("QuoteResponse").downField("QuoteData"))
    // .at("QuoteResponse")" +      QuoteResponse".at("QuoteData")    // " +      QuoteData"Or Prepare
    nestDecoder.map(d => QuoteRs(d))
  }

  given encoder: Encoder[QuoteRs] = {
    val baseEncoder: AsObject[List[JsonObject]] = deriveEncoder[List[JsonObject]]
    def nestJson(j: Json): Json                 = Json.fromJsonObject(JsonObject("QuoteResponse" -> Json.fromJsonObject(JsonObject("QuoteData" -> j))))
    val nestedObjs: Encoder[QuoteRs]            = baseEncoder.contramap[QuoteRs](b => b.quotes).mapJson(json => nestJson(json))
    nestedObjs
  }
}

/** This has quote type inside, ALL, MFund, etc... Parameterize or does all cover everything? HF */
case class Quote(
    dateTimeUTC: Instant,
    quoteStatus: String, // e.g. CLOSING
    ahFlag: Boolean,     // after hours flag
    hasMiniOptions: Boolean,
    product: ETProduct,
    detail: QuoteDetails
)

object Quote:

  given encoder: Encoder[Quote] = Encoder
    .encodeJsonObject
    .contramap(_ => JsonObject.singleton("msg", Json.fromString("QuoteEncoder NotImplemented")))

  given decoder: Decoder[Quote] = Decoder[Quote](hcursor =>

    val specificDetails: Result[QuoteDetails] =
      hcursor
        .keys
        .fold(List.empty)(_.toList)
        .collectFirst[Result[QuoteDetails]] {
          case "MutualFund" => hcursor.downField("MutualFund").as[MutualFund]
          case "IntraDay"   => hcursor.field("IntraDay").as[IntraDayQuoteDetails]
          case "Week52"     => hcursor.field("Week52").as[Week52]
          case "All"        => hcursor.downField("All").as[AllDetails]
        } match {
        case Some(value) => value
        case _           => throw Throwable("No Known Details Field Name Found")
      }

    val normal: Result[Quote] =
      for {
        details  <- specificDetails
        dateTime <- hcursor.downField("dateTimeUTC").as[Instant]
        qstatus  <- hcursor.downField("quoteStatus").as[String]
        flag     <- hcursor.downField("ahFlag").as[Boolean]
        options  <- hcursor.downField("hasMiniOptions").as[Boolean]
        product  <- hcursor.downField("Product").as[ETProduct] // Note: Capital P
      } yield Quote(dateTime, qstatus, flag, options, product, details)

    scribe.error(s"SCREAMING: $normal")
    normal
  )

end Quote
