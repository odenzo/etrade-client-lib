package com.odenzo.etrade.models.responses

import cats.data.NonEmptyList
import com.odenzo.etrade.base.CirceUtils
import com.odenzo.etrade.models.{PortfolioTotals, Position}
import io.circe.{Codec, Decoder}
import io.circe.generic.AutoDerivation
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder}

// Tempted to make this NonEmptyList
case class PortfolioRs(Totals: Option[PortfolioTotals], AccountPortfolio: NonEmptyList[AccountPortfolio])

object PortfolioRs {

  given Decoder[PortfolioRs] = deriveDecoder[PortfolioRs].at("PortfolioResponse")
}

// Need to upcase position
case class AccountPortfolio(
    accountId: String,
    position: List[Position],
    next: Option[String],
    nextPageNo: Option[String],
    totalPages: Option[Long]
)

// Really need to deal with { PortfolioResponse { AccountPortfolio [ { List(AccountPortfilio }}
// CIRCE Decoder something like

object AccountPortfolio {

  import com.odenzo.etrade.models.codecs.given
  val rename                                    = Map("position" -> "Position")
  given codec: Codec.AsObject[AccountPortfolio] = CirceUtils.renamingCodec(deriveCodec[AccountPortfolio], rename)
}
