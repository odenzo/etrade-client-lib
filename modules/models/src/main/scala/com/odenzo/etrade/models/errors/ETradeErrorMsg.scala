package com.odenzo.etrade.models.errors

import io.circe.*
import io.circe.syntax.*

/** Leaveing stack trace for now.  Models the XML error mesage in standard form */

case class ETradeErrorRs(rqSummary: String, errors: List[ETradeErrorMsg]) extends Throwable {
  override def getMessage: String = errors.asJson.spaces4
}

case class ETradeErrorMsg(code: Int, message: String) derives Codec.AsObject
