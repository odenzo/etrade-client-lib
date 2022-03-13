package com.odenzo.etrade.models

import cats.data.Chain
import io.circe.*

/** MF_DETAIL can only be used on valid mutual funds apparently */
enum QuoteDetail:
  case ALL, FUNDAMENTAL, INTRADAY, OPTIONS, WEEK_52, MF_DETAIL

enum QuoteStatus:
  case REALTIME, DELAYED, CLOSING, EH_REALTIME, EH_BEFORE_OPEN, EH_CLOSED

case class NetAssets(value: Long, asOfDate: ETimestamp)

case class Redemption(
    minMonth: String,               // 	string	The minimum month for redemption of mutual fund shares.
    feePercent: String,             // 		string	Fee percent charged to user by fund for redemption of mutual fund shares.
    isFrontEnd: String,             // 		string	If the value is '1' it indicated that the fund is front end load.
    frontEndValues: Chain[Values],  //	Potential values are low, high, and percent.Low denotes the lower timeline for the particular
    //  period of the fund.High denotes the higher timeline for the particular period of the fund.Percent denotes the percent that will be
    // charged between the lower and higher timeline for that particular period
    redemptionDurationType: String, // 		string	If the value is 4, time line is represented in years.If the value is 3, time line is represented in months.If the value is 10, time line is represented in days.
    isSales: String,                // 		string	This value indicates whether the fund is back end load function.
    salesDurationType: String,      // 		string	If the value is 4, time line is represented in years. If the value is 3, time line is represented in months. If the value is 10, time line is represented in days.
    salesValues: Chain[Values]      // 	array[Values]	Potential values are low, high, and percent.Low denotes the lower timeline for the

    //  particular  period of
    // the fund.High denotes the higher timeline for the particular period of the fund.Percent denotes the percent that will be charged
    // between the lower and higher timeline for that particular period.)
) derives Codec.AsObject

case class SalesChargeValues(lowhigh: String, percent: BigDecimal) derives Codec.AsObject

case class Values(
    low: String,    //	string	When the dollar amount of mutual fund purchases reaches a specified level, the sales load decreases. This field stores the minimum investment level at which the discount becomes available.
    high: String,   //	string	The maximum investment level at which the discount becomes available
    percent: String //	string	The sales load percentage for amounts between the low and high values
) derives Codec.AsObject
