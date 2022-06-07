package com.odenzo.etrade.models

import io.circe.*
import io.circe.generic.*
import io.circe.generic.auto.*

enum LoginStatus derives Codec.AsObject:
  case LOGGED_IN
  case LOGGED_OUT
  case PENDING
  case FAILED(msg: String)
