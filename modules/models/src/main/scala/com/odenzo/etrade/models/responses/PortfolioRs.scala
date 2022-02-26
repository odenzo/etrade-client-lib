package com.odenzo.etrade.models.responses

import com.odenzo.base.CirceUtils
import com.odenzo.etrade.models.Position
import io.circe.{Codec, Decoder}
import io.circe.generic.AutoDerivation
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.generic.semiauto.deriveDecoder

case class PortfolioRs(AccountPortfolio: List[AccountPortfolio])
case class AccountPortfolio(accountId: String, Position: List[Position], totalPages: Long)

// Really need to deal with { PortfolioResponse { AccountPortfolio [ { List(AccountPortfilio }}
// CIRCE Decoder something like

object PortfolioRs {

  implicit val decode: Decoder[PortfolioRs] = deriveDecoder[PortfolioRs].at("AccountPortfolio")
}

object AccountPortfolio {
  implicit val config: Configuration                   = CirceUtils.customMemberConfig(Map("position" -> "Position"))
  implicit val codec: Codec.AsObject[AccountPortfolio] = deriveConfiguredCodec[AccountPortfolio]
}
