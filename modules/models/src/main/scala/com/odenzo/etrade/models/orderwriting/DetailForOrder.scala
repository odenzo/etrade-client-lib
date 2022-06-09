package com.odenzo.etrade.models.orderwriting

import cats.*
import cats.data.*
import cats.syntax.all.*
import io.circe.*
import io.circe.syntax.{given, *}

trait DetailForOrder
//
//object DetailForOrder:
//  given Encoder.AsObject[DetailForOrder] = Encoder
//    .AsObject
//    .instance {
//      case a: DetailForEquityOrder => a.asJsonObject
//      case other                   => JsonObject("Error" := "Unknown T <: DetailForOrder", "ClassOf" := other.getClass.toGenericString)
//    }
//
//  given Decoder[DetailForOrder] = List[Decoder[DetailForOrder]](
//    Decoder[DetailForEquityOrder].widen
//    // Decoder[DetailForOptionOrder].widen,
//  ).reduceLeft(_ or _)
