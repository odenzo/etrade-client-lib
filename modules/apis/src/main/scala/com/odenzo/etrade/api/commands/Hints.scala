package com.odenzo.etrade.api.commands

import io.circe.*

case class Wrapper(a: Hints, b: Options) derives Codec.AsObject

enum Hints derives Decoder, Encoder.AsObject {
  case Timeout(msec: Long)
  case FullDebig(color: String)
  case SKIP
}

enum Options derives Codec.AsObject {
  case HELLO, GOODBYE, FOO
}
