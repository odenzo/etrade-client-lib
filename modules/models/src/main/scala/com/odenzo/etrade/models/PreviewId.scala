package com.odenzo.etrade.models

import io.circe.Codec

case class PreviewId(previewid: Long, cashMargin: CashMargin) derives Codec.AsObject
