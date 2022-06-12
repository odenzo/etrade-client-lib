package com.odenzo.etrade.models

import com.odenzo.etrade.models.utils.CirceCodecs.enumCaseCICodec
import io.circe.Codec

/** Documented Currencies supported by e-trade */
enum ETCurrency:
  case USD, EUR, GBP, HKD, JPY, CAD

object ETCurrency:
  given Codec[ETCurrency] = enumCaseCICodec()
