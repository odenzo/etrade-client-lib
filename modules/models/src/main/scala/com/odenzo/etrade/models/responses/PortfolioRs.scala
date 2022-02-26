package com.odenzo.etrade.models.responses

import com.odenzo.base.CirceUtils
import com.odenzo.etrade.models.Position
import io.circe.{Codec, Decoder}
import io.circe.generic.AutoDerivation
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder}

case class PortfolioRs(AccountPortfolio: List[AccountPortfolio])

object PortfolioRs {

  given Decoder[PortfolioRs] = deriveDecoder[PortfolioRs].at("AccountPortfolio")
}

// Need to upcase position
case class AccountPortfolio(accountId: String, position: List[Position], totalPages: Long)

// Really need to deal with { PortfolioResponse { AccountPortfolio [ { List(AccountPortfilio }}
// CIRCE Decoder something like

object AccountPortfolio {

  given codec: Codec.AsObject[AccountPortfolio] = deriveCodec[AccountPortfolio]
}
