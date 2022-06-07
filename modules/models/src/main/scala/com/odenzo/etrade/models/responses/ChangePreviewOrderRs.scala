package com.odenzo.etrade.models.responses

import com.odenzo.etrade.models.*
import com.odenzo.etrade.models.utils.CirceUtils
import io.circe.*
import io.circe.generic.semiauto.{deriveCodec, deriveEncoder}
import io.circe.syntax.*

case class ChangePreviewOrderRs(previewOrderResponse: PreviewOrderResponse)

object ChangePreviewOrderRs:
  given Codec[ChangePreviewOrderRs] = CirceUtils.capitalizeCodec(deriveCodec)
