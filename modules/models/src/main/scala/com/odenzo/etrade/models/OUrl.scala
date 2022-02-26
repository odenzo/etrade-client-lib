package com.odenzo.etrade.models
import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

/** Our own URL with Circe Codecs */
case class OUrl(s: String) extends AnyVal

object OUrl {
  implicit val configuration: Configuration = Configuration.default
  implicit val codec: Codec[OUrl]           = deriveUnwrappedCodec[OUrl]
}
