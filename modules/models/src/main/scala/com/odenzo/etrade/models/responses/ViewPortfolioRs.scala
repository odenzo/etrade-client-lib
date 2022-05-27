package com.odenzo.etrade.models.responses

import cats.data.NonEmptyList
import com.odenzo.etrade.base.CirceUtils
import com.odenzo.etrade.models.{PortfolioTotals, Position}
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*

/** @param totals Is optional I think, if not requeted */
case class ViewPortfolioRs(totals: Option[PortfolioTotals], accountPortfolio: NonEmptyList[AccountPortfolio])

object ViewPortfolioRs {
  import com.odenzo.etrade.models.codecs.given
  private val c1: Codec.AsObject[ViewPortfolioRs] = CirceUtils.capitalizeCodec(deriveCodec[ViewPortfolioRs])
  given Codec.AsObject[ViewPortfolioRs]           = Codec
    .AsObject
    .from(
      decodeA = c1.at("PortfolioResponse"),
      encodeA = (c1: Encoder.AsObject[ViewPortfolioRs])
        .mapJsonObject(jo => JsonObject.singleton("PortfolioResponse", Json.fromJsonObject(jo)))
    )

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
