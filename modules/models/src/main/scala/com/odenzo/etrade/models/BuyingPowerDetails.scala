package com.odenzo.etrade.models

case class MarginBuyingPowerDetails(
    nonMarginable: OrderBuyPowerEffect, //	The total in the account that is not marginable
    marginable: OrderBuyPowerEffect     //	The total in the account that is marginable
)

case class DtBuyingPowerDetails(
    nonMarginable: OrderBuyPowerEffect, //	The total in the account that is not marginable
    marginable: OrderBuyPowerEffect     //	The total in the account that is marginable
)

case class CashBuyingPowerDetails(
    nonMarginable: OrderBuyPowerEffect, //	The total in the account that is not marginable
    marginable: OrderBuyPowerEffect     //	The total in the account that is marginable
)

case class OrderBuyPowerEffect(
    currentBp: Amount,          //	Current Buying Power, without including Open orders
    currentOor: Amount,         //	Open Order Reserve for the existing open orders
    currentNetBp: Amount,       //	Current Buying Power minus the CurrentOOR
    currentOrderImpact: Amount, //	The current order impact on the account
    netBp: Amount               //	Buying Power after factoring in the Current Order
)
