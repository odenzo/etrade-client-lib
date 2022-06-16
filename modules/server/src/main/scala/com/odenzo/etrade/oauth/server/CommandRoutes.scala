package com.odenzo.etrade.oauth.server

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.kernel.Outcome
import cats.effect.syntax.all.*
import cats.syntax.all.*
import com.odenzo.etrade.models.utils.OPrint.oprint
import com.odenzo.etrade.api.{ETradeContext, ETradeService}
import com.odenzo.etrade.api.requests.*
import com.odenzo.etrade.oauth.{OAuthClientMiddleware, OAuthSessionData, OAuthStaticSigner}

import org.http4s.*
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.dsl.io.*
import org.http4s.headers.`Content-Type`

import scala.concurrent.duration.*

/** This can be added to the server to handle sending of ETradeCommands from the front-end for backend execution. */
object BizRoutes {
  case class LoginFailed(message: String, cause: Throwable)      extends Exception
  case class LoginPending(message: String = "Not Logged In Yet") extends Exception

  /**
    * This should be used just to create Client[IO] and ETradeContext but its based on a Deffered. Not sure how to reactively make a new
    * deferred from an existing one via Map. other than "polling" whever needed with TrtGet
    */
  def handleLoginState(st: Option[Either[Throwable, OAuthSessionData]]): IO[OAuthSessionData] = {
    st match
      case Some(Right(session)) => IO.pure(session)
      case Some(Left(err))      => IO.raiseError(LoginFailed("Login Error", err))
      case None                 => IO.raiseError(LoginPending())
  }

  def routes(sessionD: Deferred[IO, Either[Throwable, OAuthSessionData]], client: Client[IO]): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case rq @ POST -> Root / "command" =>
      import org.http4s.circe.CirceEntityEncoder._
      import org.http4s.circe.CirceEntityDecoder._
      import com.odenzo.etrade.api.commands.{given, *}
      sessionD
        .tryGet
        .flatMap(handleLoginState)
        .flatMap { session =>
          given OAuthSessionData = session
          given ETradeContext    = ETradeContext(session.config.apiUrl)
          given Client[IO]       = OAuthStaticSigner(session)(client)

          val cmd: IO[ETradeCmd] = rq.as[ETradeCmd]

          // I think I wrote a Matchable based function that given T <: Base  and fn(T): JsonObject for all T
          // Find it and GIST it, part of the ETradeCmd codecs?
          // Revisit this, but I think unless we narrow it looks for typeclass instance of ETradeCmd which doesn't exist.
          // Existential types, boo. I guess we would chain decoderds and put an effect (c.exec().asJson) on each decoder.
          // But, thats really not much help. Can we automatically generate from sealed trait in inline def, *maybe*
          // With a macro for sure we can expand all this out easily for the special case we are doing the same thing after narrowing.
          //

          cmd.flatMap {
            case c: ListAccountsCmd         => Ok(c.exec())
            case c: FetchAccountBalancesCmd => Ok(c.exec())
            case c: ListTransactionsCmd     => Ok(c.exec())
            case c: FetchTxnDetailsCmd      => Ok(c.exec())
            case c: FetchQuoteCmd           => Ok(c.exec())
            case c: LookupProductCmd        => Ok(c.exec())
            case c: ViewPortfolioCmd        => Ok(c.exec())
            case c: ListAlertsCmd           => Ok(c.exec())
            case c: ListAlertDetailsCmd     => Ok(c.exec())
            case c: DeleteAlertsCmd         => Ok(c.exec())
            case c: PreviewOrderCmd         => Ok(c.exec())
            case c: ListOrdersCmd           => Ok(c.exec())
            case c: PlaceOrderCmd           => Ok(c.exec())
            case c: CancelOrderCmd          => Ok(c.exec())
            case c: GetOptionExpiryCmd      => Ok(c.exec())
            case c: GetOptionChainsCmd      => Ok(c.exec())
            case c: PlaceChangedOrderCmd    => Ok(c.exec())
            case c: ChangePreviewedOrderCmd => Ok(c.exec())

          }
        }
        .onError(e => IO(scribe.error(s"Got An Exception Handling COmmand", e)))
        .recoverWith {
          case e: LoginFailed  => Status.NetworkAuthenticationRequired(s"Failed Login to ETrade ${e.message}")
          case e: LoginPending => Status.NetworkAuthenticationRequired(s"Waiting for Login to ETrade")
        }

  }
}

object Foo {
  def withoutImpl() =
    import com.odenzo.etrade.api.{given, *}
    import com.odenzo.etrade.api.commands.{given, *}
    import com.odenzo.etrade.models.responses.{given, *}
    val cmd: LookupProductCmd                = LookupProductCmd("key")
    val res: ETradeService[cmd.RESULT]       = cmd.exec()
    val res2: ETradeService[cmd.RESULT]      = cmd.exec()
    val res3: ETradeService[LookupProductRs] = cmd.exec()

  // Ok, have literrally a  ContextFunction2[X,Y] => A
  // ANd I have another ContextFunction2[X,Y] => B
  // The most simple case
}
