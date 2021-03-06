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

  // val autoDisc = this.toString // This is CirceUtils not the embedded
  // type X        = scala.compiletime.erasedValue[T].getClass
  // scala.compiletime.error("Copmile Time Error")
  // inline val tBased: T = constValue[T]
  // inline val tName     = tBased.toString
  // scala.compiletime.error(s"Type  ${Type.show[T]} not dealt with")

  /**
    * Allows the embedding of subtype discruminators. The auto X derives Codec.AsObject doesn't seem to do this as of 14.2 I also want the
    * discriminator in (wether x:ETradeCmd).asJsonObject or (x:ListAccounts).asJsonObject
    */

  def postAddDiscriminator(key: String, myName: String)(obj: JsonObject): JsonObject = {
    obj.add(key, Json.fromString(myName))
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

  def mapKeys(mapping: Map[String, String])(s: String): String = mapping.getOrElse(s, s) // mapped(s) or s

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

}

object CirceUtils extends CirceUtils
