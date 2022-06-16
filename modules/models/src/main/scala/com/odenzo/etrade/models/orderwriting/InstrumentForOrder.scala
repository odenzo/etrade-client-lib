package com.odenzo.etrade.models.orderwriting

import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.utils.{CirceCodecs, CirceUtils}
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

/**
  * A subset of Instrument with the R/W used for placing/previewing an order. ETrade replies use Instrument. This is for writing new orders.
  * This should be good enough for Equity/Stocks and MutualFunds, probably Options too
  */
case class InstrumentForOrder(
    product: ETProduct,
    orderAction: OrderAction,
    quantityType: OrderQuantityType,
    quantity: BigDecimal,                        // This is Amount in $ or Number of shares depensding on quantityType
    lots: Option[List[Lot]] = None,              // The object for the position lot                    // Maybe for sell orders
    mfQuantity: Option[MFQuantity] = None,
    mfTransaction: Option[MFTransaction] = None, // transaction for the mutual fund order	BUY, SELL
    reserveOrder: Boolean = false,
    reserveQuantity: Option[Long] = None         // Mutual Fund Quantity
)

object InstrumentForOrder:
  given Codec.AsObject[InstrumentForOrder] = CirceCodecs.renamingCodec(Map("product" -> "Product"))
