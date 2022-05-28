package com.odenzo.etrade.oauth

import cats.effect.*
import cats.effect.syntax.all.*

import cats.*
import cats.data.*
import cats.data.Validated.*
import cats.syntax.all.*

import org.http4s.client.oauth1.ProtocolParameter.{Nonce, Timestamp}
import org.http4s.client.oauth1.Token
import org.http4s.*

import java.time.Instant // Needs scala-java-time ScalaJS Lib
import java.util.UUID    // In ScalaJS

trait OAuthHelpers {

  protected val nonce: IO[Nonce]  = IO.delay(Nonce(UUID.randomUUID().toString))
  protected val ts: IO[Timestamp] = IO.delay(Timestamp(Instant.now().getEpochSecond.toString))

  // TODO: Loosing type info on list size, new tuples should work better?
  protected def getFormVar(form: UrlForm, field: String*): ValidatedNec[String, List[String]] = field
    .toList
    .traverse { n => form.getFirst(n).fold(s"Form Var $n not found".invalidNec)(v => v.validNec) }

  /** Move to validation for better error message */
  protected def extractToken(form: UrlForm): IO[Token] = IO(getFormVar(form, "oauth_token", "oauth_token_secret")).flatMap {
    case Valid(List(token, secret)) => Token(token, secret).pure
    case Invalid(msg)               => IO.raiseError(Exception(s"Trouble Extract Auth Tokens: ${msg.toList.mkString("\n")}"))
    case Valid(l: List[String])     => IO.raiseError(Exception(s"List size ${l.size} != 2 for parameters"))
  }

  /** If the res is not success status IO raises an exception with details. */
  def raiseIfError(res: Response[IO], msg: String): IO[Unit] = IO.raiseWhen(!res.status.isSuccess)(responseToThrowable(res, msg))

  /** Raises an error (Throwable for now) no matter what with description of response */
  def responseToThrowable(res: Response[IO], msg: String): Throwable = Throwable(s"${res.status} - $msg  $res")

  /** Produces a dump with all the headers - DO NOT LOG THIS IT MAY CONTAIN SENSITIVE INFORMATION */
  def dumpRequest(rq: Request[IO]): String = {
    val auth: String = dumpHeaders(rq.headers)
    s"REQUEST Headers:\n $auth \nStandard: $rq"
  }

  /** Produces a dump with all the headers - DO NOT LOG THIS IT MAY CONTAIN SENSITIVE INFORMATION */
  def dumpResponse(rs: Response[IO]): String = s"RESPONSE Headers: ${dumpHeaders(rs.headers)} \n$rs"

  def dumpHeaders(headers: Headers): String = headers.headers.map(raw => s"Header: ${raw.name} ${raw.value}").mkString("\t", "\n", "\n")
}
