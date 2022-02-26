package com.odenzo.etrade.models
import io.circe.*
import io.circe.Codec
import io.circe.Encoder.encodeString
import io.circe.Decoder.decodeString
import io.circe.generic.AutoDerivation
import io.circe.generic.semiauto.deriveCodec

/** Our own URL with Circe Codecs Can IU kill this? */
case class OUrl(s: String)

object OUrl {
  // No URL Validation because not sure why I using this instead of HTTP Uri or Path
  val baseCodec: Codec[String] = Codec.from(decodeString, encodeString)
  given Codec[OUrl]            = baseCodec.iemap(s => Right(OUrl(s)))(_.s)
}
