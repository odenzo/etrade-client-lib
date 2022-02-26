package com.odenzo.etrade.models

import io.circe.Codec

/** int64 millies since epoch, but decoding as Instant not happy,s TODO: REVISIT */
case class ETTimestamp(click: Long) derives Codec.AsObject
