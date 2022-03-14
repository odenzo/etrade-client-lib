package com.odenzo.etrade.client.engine

import cats.*
import cats.data.*
import cats.syntax.all.*
import cats.effect.*
import cats.effect.syntax.all.*
import com.odenzo.etrade.models.responses.AccountBalanceRs
import io.circe.*
import org.http4s.*
import org.http4s.client.Client
import org.http4s.headers.*
import org.http4s.client.dsl.io.*
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
