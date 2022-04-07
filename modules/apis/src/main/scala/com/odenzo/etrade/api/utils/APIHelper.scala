package com.odenzo.etrade.api.utils

import org.http4s.{Header, Headers, MediaType, ParseResult}
import org.typelevel.ci.CIString

import java.time.format.DateTimeFormatter

/** Trait that has some helper to define the outbound calls to e-trade */
trait APIHelper {

  // def sign(rq: Request[IO])(implicit session: ETradeSession) = Authentication.signRq(rq, session)

  val ddMMUUUU: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")
  val MMddUUUU: DateTimeFormatter = DateTimeFormatter.ofPattern("MMddyyyy")

  val json: (Any, ParseResult[MediaType]) = (MediaType.application.json, MediaType.parse("text/html"))
  val acceptJsonHeaders: Headers          = Headers(Header.Raw(CIString("Accept"), "application/json"))
  val acceptPdfHeaders: Headers           = Headers(Header.Raw(CIString("Accept"), "application/pdf"))
  val acceptXmlHeaders: Headers           = Headers(Header.Raw(CIString("Accept"), "application/xml"))

}
