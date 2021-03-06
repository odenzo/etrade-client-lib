package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.*
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.syntax.*

case class PlaceOrderRs(placeOrderResponse: PlaceOrderResponse)
object PlaceOrderRs:
  given Codec.AsObject[PlaceOrderRs] = CirceUtils.capitalizeCodec(deriveCodec)

case class PlaceOrderResponse(
    orderType: OrderType,    //	string	The type of order being placed	EQ, OPTN, SPREADS, BUY_WRITES, BUTTERFLY,
    // IRON_BUTTERFLY, CONDOR, IRON_CONDOR, MF, MMF
    messageList: Messages,   //	The object for the message list
    totalOrderValue: Amount, //	number	The total order value
    totalCommission: Amount, //	number	The total commission

    order: List[Detail],              //	The details of the order
    dstFlag: Option[Boolean],         //	boolean	Indicator flag identifying whether daylight savings time is applicable or not
    optionLevelCd: Option[Int],       //	integer (int32)	The code that designates the applicable options level
    marginLevelCd: MarginLevel,       // 	string	The code that designates the applicable margin level
    isEmployee: Option[Boolean],      //	boolean	Indicator flag identifying whether user is an E*TRADE employee
    commissionMsg: String,            // 	string	The commission message
    orderIds: List[Long],
    placedTime: ETimestamp,
    accountId: String,                // 	string	The numeric account ID
    portfolioMargin: PortfolioMargin, // 	The portfolio margin details for the user
    disclosure: Disclosure,           // 	The disclosure designation
    clientOrderId: String             // A reference ID generated by the developer
) derives Codec.AsObject
