package com.odenzo.etrade.models

import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.models.opaques.NEString
import io.circe.Decoder.Result
import io.circe.Json.JNumber
import io.circe.{Codec, Decoder, DecodingFailure, Encoder, Json}
import io.circe.generic.semiauto.deriveCodec

// Percentages, Dollar Amounts, etc etc to add here.
// Going to wait until next Circe release with better support for opaque types (recursion when efined in object now on summons)
object opaques {
  opaque type NEString = Option[String]

  /** NonEmptyString - Blank strings are Option.empty otherwisse Some(s) */
  object NEString:
    def apply(s: String): NEString = Option(s).flatMap(s => Option.unless(s.isBlank)(s)) // Note that no trim on s)

    def fromOption(os: Option[String]): NEString = {
      scribe.debug(s"Applying $os")
      os.flatMap(s => apply(s))
    }

    def underlying(s: NEString): Option[String] = s

    val empty: NEString = None
    //  given decoder: Decoder[NEString]             = Decoder.instance[NEString] { hcuror =>
    //    val json = hcuror.value
    //    if json.isNull then NEString.empty.asRight[DecodingFailure]
    //    else if json.isString then NEString.fromOption(json.asString).asRight[DecodingFailure]
    //    else DecodingFailure("Invalid NEString Value", hcuror.history).asLeft[NEString]
    //
    //  }

    extension (nes: NEString)
      def asString      = underlying(nes)
      def asBlankString = nes.fold("")(identity)

  end NEString

}

object codecs {
  given neStringDecoder: Decoder[NEString] = Decoder.instance[NEString] { hcuror =>
    scribe.info(s"Decoding NEString $hcuror ${hcuror.succeeded}")
    if !hcuror.succeeded then Option.empty[String].asRight
    val raw: Result[Option[String]]               = hcuror.as[Option[String]]
    val mapped: Either[DecodingFailure, NEString] = raw.map(os => NEString.fromOption(os): NEString)
    mapped
  }

  given dneStringEncoder: Encoder[NEString] = Encoder.instance {
    case Some(value: String) => Json.fromString(value)
    case NEString.empty      => Json.Null
  }
}
