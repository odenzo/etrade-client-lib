package com.odenzo.etrade.models

import io.circe.*
import io.circe.syntax.*

enum TransactionCategory derives Codec.AsObject {
  case Trades, Withdrawals, Cash
}
