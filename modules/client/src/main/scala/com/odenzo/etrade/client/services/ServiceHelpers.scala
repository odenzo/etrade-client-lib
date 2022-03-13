package com.odenzo.etrade.client.services

import cats.syntax.all.*
import cats.data.{Chain, Ior}
import cats.effect.IO
import cats.effect.syntax.all.*
import com.odenzo.base.OPrint.oprint
import com.odenzo.etrade.client.engine.IorDecoder
import com.odenzo.etrade.models.responses.MessageRs
import io.circe.Decoder
import org.http4s.{MalformedMessageBodyFailure, Request, Response}
import org.http4s.client.Client
import org.typelevel.jawn.ParseException

trait ServiceHelpers {

  /** Thi handles both HTTP protocal level and the decoding */
  protected def errorHandlerFn[T](rq: Request[IO], rs: Response[IO], err: Throwable): IO[T] = {
    scribe.error(s"Died on Response ${oprint(rs)}")
    val body: IO[String] = rs.bodyText.compile.string
    body.flatTap(s => IO(scribe.error(s"Body:\n$s"))) *> IO.raiseError(Throwable(s"HTTP Error: ${rq.uri} ${rs.status}", err))

  }

  /** Standard expects JSON respone UNLESS 400 then expects XML Error which it throws */
  protected def standard[T: Decoder](rqIO: IO[Request[IO]])(using c: Client[IO]): IO[T] = {
    import com.odenzo.etrade.models.*
    import com.odenzo.etrade.models.responses.*
    import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder

    rqIO.flatMap { rq =>
      c.run(rq)
        .use { rs =>
          rs.as[T]
            .handleErrorWith {
              case err: MalformedMessageBodyFailure =>
                scribe.error(s"Malformed Response: ${err.details}", err)
                err
                  .cause
                  .foreach {
                    e => scribe.error("Caused By Parse", e)
                  }
                IO.raiseError(err)
              case err                              => errorHandlerFn[T](rq, rs, err)
            }
        }
    }
  }

  protected def withMessages[T: Decoder](rqIO: IO[Request[IO]])(using c: Client[IO]): IO[Ior[MessageRs, T]] = {
    import com.odenzo.etrade.models.*
    import com.odenzo.etrade.models.responses.*
    import org.http4s.circe.CirceEntityDecoder.circeEntityDecoder

    val result: IO[Ior[MessageRs, T]] = rqIO.flatMap { rq =>
      c.run(rq)
        .use { rs => // Note: If MessageRs not preent an empty MessageRs is returned instead of using optional
          (rs.as[MessageRs], rs.as[T]).parMapN((m, t) => if m.isEmpty then Ior.Right(t) else Ior.both(m, t))
        }
    }
    result
  }

  /**
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
  def loopingFunction[A, B: Decoder](
      rqFn: Option[A] => IO[Request[IO]],
      shouldLoop: B => Option[A]
  )(cursor: Option[A] = None, acc: Chain[B] = Chain.empty)(using client: Client[IO]): IO[Chain[B]] = {
    val nextRequest: IO[Request[IO]] = rqFn(cursor)
    val nextResult: IO[B]            = standard[B](nextRequest)

    val returning: IO[Chain[B]] = nextResult.flatMap { (result: B) =>
      shouldLoop(result) match {
        case None        => acc.append(result).pure
        case a @ Some(n) => loopingFunction(rqFn, shouldLoop)(a, acc.append(result))
      }
    }
    returning
  }

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

}

object ServiceHelpers extends ServiceHelpers {
  // This is empty
}
