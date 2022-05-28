package com.odenzo.etrade.api.commands

import cats.*
import cats.data.*
import cats.effect.*
import cats.effect.syntax.all.*
import cats.syntax.all.*
import io.circe.{Codec, Decoder, Encoder}

import java.util.concurrent.atomic.AtomicLong

case class CmdResponse[U](rs: U, sequence: String) derives Codec.AsObject

object CmdResponse:
  def apply[F[_], T <: ETradeCmd, U: Encoder: Decoder](seq: String, rs: U): Any = CmdResponse(rs, seq)
