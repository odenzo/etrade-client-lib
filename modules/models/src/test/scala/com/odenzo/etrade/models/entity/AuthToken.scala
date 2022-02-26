package com.odenzo.etrade.models.entity
import java.time.Instant

case class AuthToken(tokenA: String, tokenB: String, createTS: Instant, lastUsedTS: Instant)
