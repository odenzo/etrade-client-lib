//package com.odenzo.etrade.client.api
//import org.http4s.client.Client
//import org.http4s.{Request, Response}
//import cats.effect.IO
//import io.circe.{Decoder, Json}
//import com.odenzo.base.OPrint.oprint
//import org.http4s.circe.jsonDecoder
//
///** These are URLs to access services within the cluster. Make crude simula of OpenAPI type client. */
//trait UsingRestClient {
//
//  def fetch[T: Decoder](rq: IO[Request[IO]])(implicit c: Client[IO]): IO[T] = for {
//    call <- rq
//    _    <- IO(scribe.debug(s"About to Call ${call}"))
//    rs   <- c.expect[Json](call)
//    m    <- rs.as[T] match {
//              case Left(err)    => IO.raiseError(new Throwable(s"Trouble Decoding JSON \n ${rs.spaces4}", err))
//              case Right(value) => IO.pure(value)
//            }
//
//  } yield m
//
//  /** Calls the request using run with its own client created */
//  protected def call[A](rq: IO[Request[IO]])(f: Response[IO] => IO[A])(implicit client: Client[IO]): IO[A] = rq.flatMap {
//    rq =>
//      client
//        .run(rq)
//        .use { (rs: Response[IO]) => f(rs) }
//  }
//
//}
