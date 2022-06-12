package com.odenzo.etrade.models.responses

import cats.Semigroup
import cats.data.NonEmptyList
import com.odenzo.etrade.models.utils.{CirceCodecs, CirceUtils}
import com.odenzo.etrade.models.{PortfolioTotals, Position}
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import monocle.*
import monocle.syntax.all.*

/** @param totals Is optional */
case class ViewPortfolioRs(totals: Option[PortfolioTotals], accountPortfolio: List[AccountPortfolio])

object ViewPortfolioRs {
  import com.odenzo.etrade.models.codecs.given
  given Codec.AsObject[ViewPortfolioRs] = CirceUtils.nestedCapitalizeCodec(deriveCodec[ViewPortfolioRs], "PortfolioResponse")

  given Semigroup[ViewPortfolioRs] with {
    override def combine(x: ViewPortfolioRs, y: ViewPortfolioRs): ViewPortfolioRs = x
      .focus(_.accountPortfolio)
      .modify(_ ++ y.accountPortfolio)
  }
}

case class AccountPortfolio(
    accountId: String,
    position: List[Position],
    next: Option[String],
    nextPageNo: Option[String],
    totalPages: Option[Long]
)

object AccountPortfolio {

  import com.odenzo.etrade.models.codecs.given
  given codec: Codec.AsObject[AccountPortfolio] = CirceCodecs.renamingCodec(Map("position" -> "Position"))
}
