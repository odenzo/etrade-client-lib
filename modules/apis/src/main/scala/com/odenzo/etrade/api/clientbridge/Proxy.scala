package com.odenzo.etrade.api.clientbridge

import cats.effect.IO
import com.odenzo.etrade.api.requests.ETradeCmd
import com.odenzo.etrade.api.requests.MarketApi.acceptJsonHeaders
import com.odenzo.etrade.api.{ETradeCall, ETradeContext, ETradeService, baseUri}
import com.odenzo.etrade.models.LoginStatus
import com.odenzo.etrade.models.errors.ETradeErrorRs
import com.odenzo.etrade.models.responses.{*, given}

import io.circe.{Decoder, Encoder, Json}
import org.http4s.Method.POST
import org.http4s.client.Client
import org.http4s.{Request, Uri, client}
import io.circe.*
import io.circe.syntax.{*, given}
import org.http4s.circe.{*, given}

/** In this case the Client will just call local host, and the context, well, it points to set URL */
class Proxy(serverUrl: Uri, client: Client[IO], context: ETradeContext) {}

object Proxy {

  def send[T <: ETradeCmd: Encoder](cmd: T)(using dec: Decoder[cmd.RESULT], c: Client[IO]): ETradeService[cmd.RESULT] = {

    val rqIO: ETradeCall = IO.pure(
      Request[IO](POST, baseUri / "biz" / "command").withHeaders(acceptJsonHeaders).withEntity(cmd.asJson)
    )

    given Decoder[cmd.RESULT] = dec
    rqIO.flatMap { (rq: Request[IO]) =>
      import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
      scribe.info(s"Actioning Request: $rq")

      c.run(rq)
        .use { rs =>
          if rs.status.code == 200 then
            scribe.info(s"OK Code -- Decoding Body")
            rs.as[cmd.RESULT]
          else
            IO(scribe.info(s"ERROR")) *>
              rs.as[Json].flatMap(json => IO.raiseError(Throwable(s"Error At Application Level: ${rs.status}: ${json.spaces2}")))

        }
    }
  }
}
