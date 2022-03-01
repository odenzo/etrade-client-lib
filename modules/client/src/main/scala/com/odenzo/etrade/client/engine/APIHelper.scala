package com.odenzo.etrade.client.engine

import cats.effect.IO
import org.http4s.Request

import java.time.format.DateTimeFormatter

///** Trait that has some helper to define the outbound calls to e-trade */
//trait APIHelper {
//
//  def sign(rq: Request[IO])(implicit session: ETradeSession) = Authentication.signRq(rq, session)
//
//  val ddMMUUUU: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")
//  val MMddUUUU: DateTimeFormatter = DateTimeFormatter.ofPattern("MMddyyyy")
//
//}
