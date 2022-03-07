package com.odenzo.etrade.client.services
import cats.*
import cats.data.Chain
import cats.syntax.all.*
import cats.effect.{IO, Resource}
import com.odenzo.etrade.client.api.AccountsApi.{accountBalancesCF, listTransactionsCF, standardCall}
import com.odenzo.etrade.models.responses.{AccountBalanceRs, TransactionListRs}
import org.http4s.{Request, Response}
import org.http4s.client.Client
import com.odenzo.etrade.client.engine.*
import com.odenzo.etrade.models.Transaction
import io.circe.Decoder

import java.time.LocalDate

object Services extends ServiceHelpers {
  def accountBalanceApp(
      accountIdKey: String,
      accountType: Option[String] = None,
      instType: String = "BROKERAGE"
  ): ETradeService[AccountBalanceRs] = {
    val client = summon[Client[IO]]
    val rq     = accountBalancesCF(accountIdKey, accountType, instType)
    client.run(rq).use((rs: Response[IO]) => standardCall[AccountBalanceRs](rq, rs))
  }

  // Make a genericaly pattern first with a little oddnes
  def listTransactionsApp(
      accountIdKey: String,
      startDate: Option[LocalDate] = None,
      endDate: Option[LocalDate] = None,
      count: Int = 50
  )(using client: Client[IO]): ETradeService[Chain[Transaction]] = {
    given c: Client[IO]                                = client
    val rqFn: Option[String] => Request[IO]            = listTransactionsCF(accountIdKey, startDate, endDate, count, _)
    val extractor: TransactionListRs => Option[String] = (rs: TransactionListRs) => rs.transactionListResponse.marker
    loopingFunction(rqFn, extractor)(None, Chain.empty).map { (responses: Chain[TransactionListRs]) =>
      responses.flatMap(rs => rs.transactionListResponse.transaction)
    }

  }
}
