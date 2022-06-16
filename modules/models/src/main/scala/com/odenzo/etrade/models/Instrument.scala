package com.odenzo.etrade.models
import com.odenzo.etrade.models.utils.{CirceCodecs, CirceUtils}
import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

/**
  * Full Instrument, with many fields set by ETrade. See InstrumentForOrder when constructing a new Order
  * @param product
  * @param symbolDescription
  * @param orderAction
  * @param quantityType
  * @param quantity
  * @param cancelQuantity
  * @param orderedQuantity
  * @param filledQuantity
  * @param averageExecutionPrice
  * @param estimatedCommission
  * @param estimatedFees
  * @param bid
  * @param ask
  * @param lastprice
  * @param currency
  * @param lots
  * @param mfQuantity
  * @param osiKey
  * @param mfTransaction
  * @param reserveOrder
  * @param reserveQuantity
  */
case class Instrument(
    product: ETProduct,
    symbolDescription: String,
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
    currency: Option[ETCurrency],         // The currency used for the order request	USD, EUR, GBP, HKD, JPY, CAD
    lots: Option[List[Lot]],              // The object for the position lot
    mfQuantity: Option[MFQuantity],
    osiKey: Option[String],               // The Options Symbology Initiative (OSI) key w/ the option root symbol, expiration
    // date, call/put indicator, and strike price
    mfTransaction: Option[MFTransaction], // transaction for the mutual fund order	BUY, SELL
    reserveOrder: Option[Boolean],        // If TRUE,  a reserve order meaning that only a limited number of shares will be
    // publicly displayed
    reserveQuantity: Option[Long]         // The number of shares to be publicly displayed if this is a reserve order
)

object Instrument:
  given Codec.AsObject[Instrument] = CirceCodecs.renamingCodec[Instrument](Map("product" -> "Product"))
