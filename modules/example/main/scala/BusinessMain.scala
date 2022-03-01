//import com.odenzo.base.OPrint.oprint
//import com.odenzo.base.{CirceUtils, IOU}
//import com.odenzo.etrade.client.api.{APIHelper, AccountsApi, UsingRestClient}
//import com.odenzo.etrade.client.authentication.ETradeSession
//import org.http4s.client.Client
//
//import java.time.LocalDate
//import scala.io.circe.Decoder.Result
//import scala.io.circe.Json
//import scala.io.circe.generic.auto.exportEncoder
//import scala.io.circe.syntax.EncoderOps
//import scala.reflect.io.File
//import scala.util.chaining.scalaUtilChainingOps
//
//object BusinessMain extends UsingRestClient with APIHelper {
//  def afterLoginDo(expSession: ETradeSession)(implicit client: Client[IO]): IO[Unit] = {
//    implicit val runtime: IORuntime     = cats.effect.unsafe.IORuntime.global
//    implicit val session: ETradeSession = expSession
//
//    for {
//      _            <- IO(scribe.info(s"Have Access Token Etc and now doing some work..."))
//      accounts     <- AccountsApi.listAccounts
//      //  _            <- IO(scribe.info(s"Accounts ${oprint(accounts)}"))
//      accountIdKey <- accounts.accounts.headOption.map(_.accountIdKey) pipe IOU.required("accountIdKey")
//      balances     <- AccountsApi.accountBalances(accountIdKey)
//      _             = scribe.info(s"Account Balances: ${oprint(balances)}")
//      portfolio    <- AccountsApi.viewPortfolio(accountIdKey)
//      _             = scribe.info(s"PORTFOLIO: ${oprint(portfolio)}")
//      // txns         <- AccountsApi.listTransactions(accountIdKey)
//    } yield ()
//
//  }
//
//  def downloadAllTxn(expSession: ETradeSession)(implicit client: Client[IO]): IO[Unit] = {
//    implicit val runtime: IORuntime     = cats.effect.unsafe.IORuntime.global
//    implicit val session: ETradeSession = expSession
//
//    val accountKey: IO[String] = for {
//      _            <- IO(scribe.info(s"Have Access Token Etc and now doing some work..."))
//      accounts     <- AccountsApi.listAccounts
//      //  _            <- IO(scribe.info(s"Accounts ${oprint(accounts)}"))
//      accountIdKey <- accounts.accounts.headOption.map(_.accountIdKey) pipe IOU.required("accountIdKey")
//      //  _             = scribe.info(s"Account Key ID: $accountIdKey")
//      _            <- getMonth(accountIdKey, LocalDate.of(2018, 11, 1))
//    } yield accountIdKey
//    accountKey.void
//  }
//
//  def getMonth(accountKey: String, start: LocalDate)(implicit s: ETradeSession, c: Client[IO]): IO[Unit] = {
//
//    val end = start.plusMonths(1)
//    if (!start.isBefore(LocalDate.of(2019, 1, 1))) then IO.unit
//    else
//      for {
//        txns <- AccountsApi.listTransactions(accountKey, start.some, end.some)
//        _    <- CirceUtils.writeJson(txns, new java.io.File(s"txns_${start.toString}.json")).to[IO]
//        _     = scribe.info(s"Start $start has More: ${needToPage(txns)}")
//        _    <- getMonth(accountKey, end)
//      } yield ()
//  }
//
//  def needToPage(json: Json): Boolean = {
//    (json \\ "moreTransactions").headOption match {
//      case None    => throw new Exception("moreTransactions field not found")
//      case Some(h) => h.asBoolean.getOrElse(throw new Exception(s"Invalid JSON for Bool ${h.spaces4}"))
//    }
//  }
//}
