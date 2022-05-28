package com.odenzo.etrade.api.commands

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.given
//import io.circe.generic.auto.*, io.circe.syntax.*
import java.util.concurrent.atomic.AtomicLong

case class CmdEnvelope[T <: ETradeCmd](cmd: T, sequence: String) derives Codec.AsObject

object CmdEnvelope {
  private val counter = new AtomicLong()

  def nextSequence[F[_]]()(using F: Sync[F]): F[Long] = F.delay(counter.incrementAndGet())

  def apply[F[_], T <: ETradeCmd: Encoder: Decoder](t: T)(using F: Sync[F])(using Codec[T]): Any =
    for {
      seq <- nextSequence()
      obj  = CmdEnvelope(t, seq.toString)
    } yield obj

}
