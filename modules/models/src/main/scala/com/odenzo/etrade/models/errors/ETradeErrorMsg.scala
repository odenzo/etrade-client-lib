package com.odenzo.etrade.models.errors

import io.circe.*
import io.circe.syntax.*

import scala.xml.*

/** Leaveing stack trace for now.  Modelss the XML error mesage in standard form */

case class ETradeErrorRs(rqSummary: String, errors: List[ETradeErrorMsg]) extends Throwable {
  override def getMessage: String = errors.asJson.spaces4
}

case class ETradeErrorMsg(code: Int, message: String) derives Codec.AsObject
