package com.odenzo.etrade.models.utils

import io.circe.*
import io.circe.Decoder.decodeString
import io.circe.Encoder.encodeString
import io.circe.generic.semiauto.*
import io.circe.syntax.*

import scala.compiletime.constValue
import scala.deriving.Mirror
import scala.quoted.Type

trait CirceUtils {

  final val discriminatorKey = "_I_AM_"

  /** This must be inline or Mirror can't get a constant T */
  inline def deriveDiscCodec[T](d: String)(using m: Mirror.Of[T]): Codec.AsObject[T] = {
    // val autoDisc = this.toString // This is CirceUtils not the embedded
    // type X        = scala.compiletime.erasedValue[T].getClass
    // scala.compiletime.error("Copmile Time Error")
    // inline val tBased: T = constValue[T]
    // inline val tName     = tBased.toString
    // scala.compiletime.error(s"Type  ${Type.show[T]} not dealt with")
    scribe.info(s"WhatHappened: $d ==?")
    val dec: Decoder[T]          = deriveDecoder[T] // Note: This doesn't care if discriminator is there or not intentionally
    val enc: Encoder.AsObject[T] = deriveEncoder[T].mapJsonObject(jo => jo.add(discriminatorKey, Json.fromString(d)))
    Codec.AsObject.from(dec, enc)
  }

  /**
    * Allows the embedding of subtype discruminators. The auto X derives Codec.AsObject doesn't seem to do this as of 14.2 I also want the
    * discriminator in (wether x:ETradeCmd).asJsonObject or (x:ListAccounts).asJsonObject
    */

  def postAddDiscriminator(myName: String)(obj: JsonObject): JsonObject = {
    obj.add(CirceUtils.discriminatorKey, Json.fromString(myName))
  }

  val unCaptialize: String => String =
    s =>
      val res =
        if s == null then null
        else
          s.headOption match {
            case Some(v) if v.isUpper => s.drop(1).prepended(v.toLower)
            case _                    => s
          }
      scribe.info(s"Uncapitalized Key: $s => $res")
      res

  val capitalize: String => String = (s: String) => s.capitalize

  def mapKeys(mapping: Map[String, String])(s: String): String = mapping.get(s).fold(s)(n => n)

  /** Make sure this is done eagerly, the inverse used for changing from Json Key Names to case class field names */
  def reverse(mapping: Map[String, String]): Map[String, String] = {
    assert(mapping.values.toSet.size == mapping.size, "There should be no duplicate values in table")
    mapping.toList.map(_.swap).to(Map)
  }

  def liftJsonObject(to: String)(obj: JsonObject): JsonObject = {
    JsonObject.singleton(to, Json.fromJsonObject(obj))
  }

  /** Applies fn to all keys in the Json which should be a JsonObject */
  def transformKeys(fn: String => String)(obj: Json): Json = obj.mapObject { o =>
    JsonObject.fromIterable(o.toIterable.map((k, v) => fn(k) -> v))
  }

  def prepareKeys(fn: String => String)(cursor: ACursor): ACursor = cursor.withFocus(transformKeys(fn))

  def encoderTransformKey(fn: String => String)(obj: JsonObject): JsonObject = transformKeys(fn)
    .compose(Json.fromJsonObject)
    .andThen(json => json.asObject.get)
    .apply(obj)

  /** Map is in form  case class field name => JSON field name (eg product -> Product) */
  def renamingCodec[T](codec: Codec.AsObject[T], rename: Map[String, String]): Codec.AsObject[T] = Codec
    .AsObject
    .from(
      codec.prepare(prepareKeys(mapKeys(reverse(rename)))),
      codec.mapJsonObject(encoderTransformKey(mapKeys(rename)))
    )

  /** Converts all case class fields to Upper-Case Json Field Names */
  def capitalizeCodec[T](codec: Codec.AsObject[T]): Codec.AsObject[T] = Codec
    .AsObject
    .from(
      decodeA = codec.prepare(prepareKeys(unCaptialize)),
      encodeA = codec.mapJsonObject(encoderTransformKey(capitalize))
    )

  /** Converts all case class fields to Upper-Case Json Field Names */
  def nestedCapitalizeCodec[T](codec: Codec.AsObject[T], at: String): Codec.AsObject[T] = Codec
    .AsObject
    .from(
      decodeA = codec.prepare(prepareKeys(unCaptialize)).at(at),
      encodeA = {
        val mapper: JsonObject => JsonObject = encoderTransformKey(capitalize) andThen liftJsonObject(at)
        codec.mapJsonObject(mapper)
      }
    )

// TODO: Stuck on generic enum codec for simple Value
//  def stringEnumCodec[T <: Product]                                   = {
//    Codec.from[T](
//      decodeString.emapTry(s => scala.util.Try { T.valueOf(s) }),
//      encodeString.contramap(en => en.toString)
//    )
//  }
//
  /* EnumValues.scala */

}

object CirceUtils extends CirceUtils
