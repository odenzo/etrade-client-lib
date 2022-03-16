package com.odenzo.etrade.oauth.utils
import cats.effect.*
import org.http4s.CacheDirective.public
import org.http4s.client.oauth1.ProtocolParameter.*
import org.http4s.client.oauth1.{Consumer, Token, *}
import org.http4s.client.{Client, oauth1}
import org.http4s.headers.Location
import org.http4s.*

import org.http4s.client.{Client, oauth1}
trait OAuthUtils {

  /**
    * If the res is not success status IO raises an exception with details.
    * @param res
    * @param msg
    */
  def raiseIfError(res: Response[IO], msg: String): IO[Unit] = IO.raiseWhen(!res.status.isSuccess)(responseToThrowable(res, msg))

  /** Raises an error (Throwable for now) no matter what with description of response */
  def responseToThrowable(res: Response[IO], msg: String): Throwable =
    Throwable(s"${res.status} - $msg  $res")

  /** Produces a dump with all the headers - DO NOT LOG THIS IT MAY CONTAIN SENSITIVE INFORMATION */
  def dumpRequest(rq: Request[IO]): String = {
    val auth: String = rq.headers.headers.map(raw => s"Header: ${raw.name} ${raw.value}").mkString("\t", "\n", "\n")
    s"REQUEST Headers:\n $auth \nStandard: $rq"
  }

  /** Produces a dump with all the headers - DO NOT LOG THIS IT MAY CONTAIN SENSITIVE INFORMATION */
  def dumpResponse(rs: Response[IO]): String = {
    val auth: String = rs.headers.headers.map(raw => s"Header: ${raw.name} ${raw.value}").mkString("\t", "\n", "\n")
    s"RESPONSE Headers:\n $auth \nStandard: $rs"
  }
}