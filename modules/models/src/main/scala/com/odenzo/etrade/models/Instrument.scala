package com.odenzo.etrade.models
import io.circe.Codec

case class Instrument(
    product: ETProduct,
    symboldDescription: String,
    orderAction: OrderAction,
    quantityType: OrderQuantityType,
    quantity: Option[BigDecimal],
    cancelQuantity: Option[BigDecimal],
    orderedQuantity: Option[BigDecimal],
    filledQuantity: Option[BigDecimal],
    averageExecutionPrice: BigDecimal,    // The average execution price
    estimatedCommission: BigDecimal,      // The cost billed to the user to perform the requested action
    estimatedFees: BigDecimal,            // The cost or proceeds, including broker commission, resulting from the requested action
    bid: Option[Amount],
    ask: Option[Amount],
    lastprice: Option[Amount],
    currency: ETCurrency,                 // The currency used for the order request	USD, EUR, GBP, HKD, JPY, CAD
    lots: List[Lot],                      // The object for the position lot
    mfQuantity: Option[MFQuantity],
    osiKey: String,                       // The Options Symbology Initiative (OSI) key containing the option root symbol, expiration
    // date, call/put indicator, and strike price
    mfTransaction: Option[MFTransaction], // transaction for the mutual fund order	BUY, SELL
    reserveOrder: Boolean,                // If TRUE,  a reserve order meaning that only a limited number of shares will be publicly displayed
    reserveQuantity: Option[Long]         // The number of shares to be publicly displayed if this is a reserve order
) derives Codec.AsObject
