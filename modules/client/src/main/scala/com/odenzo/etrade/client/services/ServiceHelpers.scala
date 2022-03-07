package com.odenzo.etrade.client.services

import cats.data.Chain
import cats.effect.IO
import com.odenzo.etrade.client.api.AccountsApi.standardCall
import io.circe.Decoder
import org.http4s.{Request, Response}
import org.http4s.client.Client

trait ServiceHelpers {

  /**
    * @param rqFn
    *   Function that takes a "token" or cursor to construct a paging request.
    * @param shouldLoop
    *   Returns curser for next paging request or None if at end of paging.
    * @param cursor
    *   Cursor that varies over recursion
    * @param acc
    *   Accumulator that collects all the responses (1 per paging call). Usually want to manually combine these later.
    * @tparam A
    * @tparam B
    * @return
    *   All the paging results as ordered
    */
  def loopingFunction[A, B: Decoder](
      rqFn: Option[A] => Request[IO],
      shouldLoop: B => Option[A]
  )(cursor: Option[A] = None, acc: Chain[B] = Chain.empty)(using client: Client[IO]): IO[Chain[B]] = {
    scribe.warn(s"*****\n*****\n \t $cursor ${acc.size}")
    val nextRequest: Request[IO] = rqFn(cursor)
    val nextResult: IO[B]        = client.run(nextRequest).use((rs: Response[IO]) => standardCall[B](nextRequest, rs))

    val returning: IO[Chain[B]] = nextResult.flatMap { (result: B) =>
      shouldLoop(result) match {
        case None        =>
          val aggr = acc.append(result)
          IO(scribe.info(s"**** Done Loops with Count ${aggr.size}"))
          IO.pure(aggr)
        case a @ Some(n) => loopingFunction(rqFn, shouldLoop)(a, acc.append(result))
      }
    }
    returning
  }

}

object ServiceHelpers extends ServiceHelpers
