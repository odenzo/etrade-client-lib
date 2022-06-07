package com.odenzo.etrade.models

import io.circe.Codec

/**
  * @param id
  * @param size
  *   Size of the lot in number of shares/funds
  */
case class Lot(id: Long, size: BigDecimal) derives Codec.AsObject
case class Lots(lots: List[Lot]) derives Codec.AsObject
