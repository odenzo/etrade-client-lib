package com.odenzo.etrade.models

import cats.data.Chain
import com.odenzo.etrade.base.CirceCodecs.*
import io.circe.*
import io.circe.generic.semiauto.deriveCodec
import io.circe.Decoder.*
import io.circe.Encoder.*
import io.circe.Codec.*

import scala.util.Try

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
