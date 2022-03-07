package com.odenzo.etrade.client.engine

import cats.effect.*
import cats.effect.syntax.all.*
import com.odenzo.etrade.client.api.AccountsApi.standardCall
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

  val acceptJson: Accept           = Accept(MediaType.application.json)
  val acceptJsonHeader: Header.Raw = Header.Raw(CIString("Accept"), "application/json")

  def handleHttpErrors[T](rq: Request[IO], rs: Response[IO]): IO[Throwable] = {
    IO(Throwable(s"Crude HTTP Error: ${rs.status}"))
  }

  def errorHandlerFn[T](rq: Request[IO], rs: Response[IO], err: Throwable): IO[T] = {
    IO.raiseError(Throwable(s"Crude HTTP Error: ${rs.status}", err))
  }

  def standardCall[T: Decoder](rq: Request[IO], rs: Response[IO]): IO[T] =
    import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
    rs.as[T].handleErrorWith((err: Throwable) => errorHandlerFn(rq, rs, err))

}
