package com.odenzo.etrade.models

import io.circe.Codec

/**
  * Mutual Fund Order Quantity
  *
  * @param cash
  *   Out of Cash
  * @param margin
  *   Using Margin
  * @param cusip
  *   identifier of the mutual fund (TODO: OType
  */
case class MFQuantity(cash: Amount, margin: Amount, cusip: String) derives Codec.AsObject
