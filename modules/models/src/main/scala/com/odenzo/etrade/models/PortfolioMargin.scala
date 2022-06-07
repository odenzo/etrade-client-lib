package com.odenzo.etrade.models

case class PortfolioMargin(
    houseExcessEquityNew: BigDecimal,   // 	number (double)	The new house excess equity value for portfolio-margin
    pmEligible: Boolean,                //	boolean	The new house excess equity value for portfolio-margin eligible accounts
    houseExcessEquityCurr: BigDecimal,  // 	number (double)	The current house excess equity value for portfolio-margin eligible accounts
    houseExcessEquityChange: BigDecimal //	number (double)	The change house excess equity value for portfolio-margin eligible accounts
)
