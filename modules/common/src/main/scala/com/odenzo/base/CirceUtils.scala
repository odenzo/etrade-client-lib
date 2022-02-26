package com.odenzo.base

import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*

import scala.deriving.Mirror

trait CirceUtils {

  val unCaptialize: String => String = s =>
    if s == null then null
    else
      s.headOption match {
        case Some(v) if v.isUpper => s.drop(1).prepended(v.toLower)
        case _                    => s
      }

  val capitalize: String => String = (s: String) => s.capitalize

  def mapKeys(mapping: Map[String, String])(s: String): String = mapping.get(s).fold(s)(n => n)

  /** Make sure this is done eagerly, the inverse used for changing from Json Key Names to case class field names */
  def reverse(mapping: Map[String, String]): Map[String, String] = {
    assert(mapping.values.toSet.size == mapping.size, "There should be no duplicate values in table")
    mapping.toList.map(_.swap).to(Map)
  }

  /** Applies fn to all keys in the Json which should be a JsonObject */
  def transformKeys(fn: String => String)(obj: Json): Json =
    obj.mapObject { o => JsonObject.fromIterable(o.toIterable.map((k, v) => fn(k) -> v)) }

  def prepareKeys(fn: String => String)(cursor: ACursor): ACursor =
    cursor.withFocus(transformKeys(fn))

  def encoderTransformKey(fn: String => String)(obj: JsonObject): JsonObject =
    transformKeys(fn)
      .compose(Json.fromJsonObject)
      .andThen(json => json.asObject.get)
      .apply(obj)

  def renamingCodec[T](codec: Codec.AsObject[T], rename: Map[String, String]): Codec.AsObject[T] =
    Codec.AsObject.from(
      codec.prepare(prepareKeys(mapKeys(reverse(rename)))),
      codec.mapJsonObject(encoderTransformKey(mapKeys(rename)))
    )
}

object CirceUtils extends CirceUtils
