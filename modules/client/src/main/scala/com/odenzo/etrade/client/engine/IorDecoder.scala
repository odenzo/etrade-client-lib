package com.odenzo.etrade.client.engine

import cats.*
import cats.data.*
import cats.syntax.all.*
import io.circe.Decoder.{Result, keyMissingNone}
import io.circe.{Decoder, DecodingFailure, FailedCursor, HCursor}

object IorDecoder {

  /**
    * Decoder for IOR such that if the key for left or right is NOT THERE its None, but if the key is there are the Decoder for A (or B)
    * fails. If neither are present then its an Parsing Failure Result[Ior[A,B]] is the result from aplpying decoder. If both Decoders
    * fails, you only get the first one that failed. I don't result there is semigroup for Failure besides treating as assumulating.
    */
  final def decodeIor[A, B](leftKey: String, rightKey: String)(using decodeA: Decoder[A], decodeB: Decoder[B]): Decoder[Ior[A, B]] =
    new Decoder[Ior[A, B]] {
      private[this] def failure(c: HCursor): Decoder.Result[Ior[A, B]] = Left(DecodingFailure("[A, B]Either[A, B]", c.history))

      final def apply(c: HCursor): Result[Ior[A, B]] = {
        val lf = c.downField(leftKey)
        val rf = c.downField(rightKey)

        val left: Option[Result[A]] =
          lf match {
            case cursor: HCursor      => Some(cursor.as[A])
            case cursor: FailedCursor => None
          }

        val right: Option[Result[B]]                =
          rf match {
            case cursor: HCursor      => Some(cursor.as[B]) // Need to check if null cursor?
            case cursor: FailedCursor => None
          }

        (left, right) match {
          case (None, Some(b))                  => b.map((v: B) => Ior.right[A, B](v))
          case (Some(a), None)                  => a.map(Ior.Left[A](_))
          case (Some(Right(a)), Some(Right(b))) => Ior.Both(a, b).asRight[DecodingFailure]
          case (Some(Left(a)), Some(Right(b)))  => a.asLeft
          case (Some(Left(a)), Some(Left(b)))  => a.asLeft
          case (Some(Right(a)), Some(Left(b)))  =>            b.asLeft
          case (None, None) => DecodingFailure("Ior Decoding - Neither $leftKey or $rightKey was found", c.history).asLeft[Ior[A, B]]

      }
    }
}
}
