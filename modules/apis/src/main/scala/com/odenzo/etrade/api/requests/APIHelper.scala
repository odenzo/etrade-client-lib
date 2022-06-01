package com.odenzo.etrade.api.requests

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.models.utils.OPrint.oprint
import com.odenzo.etrade.models.StoreId
import com.odenzo.etrade.models.errors.{ETradeErrorMsg, ETradeErrorRs}
import com.odenzo.etrade.models.responses.MessageRs
import io.circe.Decoder
import org.http4s.client.Client
import org.http4s.*
import org.typelevel.ci.CIString

import java.time.format.DateTimeFormatter
import scala.util.Try
import scala.xml.Elem

/** Trait that has some helper to define the outbound calls to e-trade */
trait APIHelper {

  // def sign(rq: Request[IO])(implicit session: ETradeSession) = Authentication.signRq(rq, session)

  val ddMMUUUU: DateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyy")
  val MMddUUUU: DateTimeFormatter = DateTimeFormatter.ofPattern("MMddyyyy")

  val json: (Any, ParseResult[MediaType]) = (MediaType.application.json, MediaType.parse("text/html"))
  val acceptJsonHeaders: Headers          = Headers(Header.Raw(CIString("Accept"), "application/json"))
  val acceptPdfHeaders: Headers           = Headers(Header.Raw(CIString("Accept"), "application/pdf"))
  val acceptXmlHeaders: Headers           = Headers(Header.Raw(CIString("Accept"), "application/xml"))

  protected def handleHttpErrors[T](rq: Request[IO], rs: Response[IO]): IO[Throwable] = {
    IO(Throwable(s"Crude HTTP Error: ${rs.status}"))
  }

  protected def standardCall[T: Decoder](rq: Request[IO], rs: Response[IO]): IO[T] =
    import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
    rs.as[T].handleErrorWith((err: Throwable) => errorHandlerFn(rq, rs, err))

  /** Thi handles both HTTP protocal level and the decoding */
  protected def errorHandlerFn[T](rq: Request[IO], rs: Response[IO], err: Throwable)(using F: Async[IO]): IO[T] = {
    scribe.error(s"Died on Response ${oprint(rs)}")
    val body: IO[String] = rs.bodyText.compile.string
    body.flatTap(s => IO(scribe.error(s"Body:\n$s"))) *> IO.raiseError(Throwable(s"HTTP Error: ${rq.uri} ${rs.status}", err))

  }

  /** Standard expects JSON respone UNLESS 400 then expects XML Error which it throws */
  protected def standard[T: Decoder](rqIO: IO[Request[IO]])(using c: Client[IO]): IO[T] = {
    rqIO.flatMap((rq: Request[IO]) => standardV2[T](rq))
  }

  protected def standardV2[T: Decoder](rq: Request[IO])(using c: Client[IO]): IO[T] = {
    import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

    c.run(rq)
      .use { rs =>

        val rsCode      = rs.status.code
        val contentType = rs.contentType
        rs.status.responseClass match {
          case org.http4s.Status.ClientError => // if contentType is XML otherwise try Json Messages
            IO.raiseError(Throwable(s"ClientError: ${rs.status} not dealing with XML again yet. "))
          //              import org.http4s.scalaxml.*
          //              rs.as[Elem].map(elem => errorXmlRsFromXml(rq, rs, elem)).flatMap(err => IO.raiseError(err))
          case org.http4s.Status.ServerError =>
            rs.as[String] *> IO.raiseError(ETradeErrorRs(rq.toString, List.empty)) // Actually  its text/html,

          case org.http4s.Status.Informational => rs.as[T]
          case _                               => rs.as[T]
        }
      }
  }

  protected def withMessages[T: Decoder](rqIO: IO[Request[IO]])(using c: Client[IO]): IO[Ior[MessageRs, T]] = {

    import com.odenzo.etrade.models.responses.*
    import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder
    import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
    val result: IO[Ior[MessageRs, T]] = rqIO.flatMap { rq =>
      c.run(rq)
        .use { rs => // Note: If MessageRs not preent an empty MessageRs is returned instead of using optional
          (rs.as[MessageRs], rs.as[T]).parMapN((m, t) => if m.isEmpty then Ior.Right(t) else Ior.both(m, t))
        }
    }
    result
  }

  /**
    * This returns all N paging responses. Often we have a special first response and then a way to merge subsequent responses into first
    * response. Should make one for that special case.
    * @param rqFn
    *   Function that takes a "token" or cursor to construct a paging request.
    * @param shouldLoop
    *   Returns curser for next paging request or None if at end of paging.
    * @param cursor
    *   Cursor that varies over recursion
    * @param acc
    *   Accumulator that collects all the responses (1 per paging call). Usually want to manually combine these later.
    *
    * @return
    *   All the paging results as ordered
    */
  protected def iteratePages[A, B: Decoder](
      rqFn: Option[A] => IO[Request[IO]],
      shouldLoop: B => Option[A]
  )(cursor: Option[A] = None, acc: Chain[B] = Chain.empty)(using client: Client[IO]): IO[Chain[B]] = {
    val nextRequest: IO[Request[IO]] = rqFn(cursor)
    val nextResult: IO[B]            = standard[B](nextRequest)
    val returning: IO[Chain[B]]      = nextResult.flatMap { (result: B) =>
      shouldLoop(result) match {
        case None        => acc.append(result).pure
        case a @ Some(n) => iteratePages(rqFn, shouldLoop)(a, acc.append(result))
      }
    }
    returning
  }

  protected def iterateReducingPages[A, B: Decoder: Semigroup](
      rqFn: Option[A] => IO[Request[IO]],
      shouldLoop: B => Option[A]
  )(using client: Client[IO]) = {

    def looper(cursor: Option[A], acc: Option[B]): IO[B] = {
      for {
        rq      <- rqFn(cursor)
        result  <- standardV2[B](rq)
        nextPage = shouldLoop(result)
        folded   =
          acc match {
            case None    => result
            case Some(p) => Semigroup[B].combine(p, result)
          }
        cont    <- if nextPage.isEmpty then IO.pure(folded) else looper(nextPage, Some(folded))
      } yield cont
    }
    looper(None, None)
  }

  // https://github.com/precog/matryoshka not updated yet,
  // https://github.com/higherkindness/droste IS THOUGH!! Switch over to that with standard cat theory names
  def loopingMsgFunction[A, B: Decoder](
      rqFn: Option[A] => IO[Request[IO]],
      shouldLoop: B => Option[A]
  )(cursor: Option[A] = None, acc: Chain[Ior[MessageRs, B]] = Chain.empty)(using client: Client[IO]): IO[Chain[Ior[MessageRs, B]]] = {

    val nextRequest: IO[Request[IO]]      = rqFn(cursor)
    val nextResult: IO[Ior[MessageRs, B]] = withMessages[B](nextRequest)

    nextResult.flatMap { (result: Ior[MessageRs, B]) =>
      result.right.flatMap(shouldLoop) match {
        case None        => acc.append(result).pure
        case a @ Some(n) => loopingMsgFunction(rqFn, shouldLoop)(a, acc.append(result))
      }
    }
  }

  // This is empty
  protected def errorXmlRsFromXml(rq: Request[IO], rs: Response[IO], xml: Elem): ETradeErrorRs = {
    // A far as I know only one elem in most responses
    val errs = List(errorMsgFromXml(xml))
    ETradeErrorRs(rq.toString, errs)
  }

  /** Falls back to -1 for error code and fallback for text too */
  protected def errorMsgFromXml(elem: Elem): ETradeErrorMsg = {
    val code: Int   = Try { (elem \ "code").text.toInt }.getOrElse(-1)
    val msg: String = Try { (elem \ "message").text }.getOrElse("No Message Text Found")
    ETradeErrorMsg(code, msg)
  }

  given QueryParamEncoder[com.odenzo.etrade.models.StoreId] = QueryParamEncoder
    .longQueryParamEncoder
    .contramap[StoreId]((x: StoreId) => x.toLong)
}
