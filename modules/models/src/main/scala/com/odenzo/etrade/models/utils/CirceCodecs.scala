package com.odenzo.etrade.models.utils

trait CirceCodecs extends CirceUtils {
  import org.typelevel.ci.CIString
  import io.circe.*
  import io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}
  import scala.compiletime.summonAll
  import scala.deriving.Mirror

  /*  // val autoDisc = this.toString // This is CirceUtils not the embedded
    // type X        = scala.compiletime.erasedValue[T].getClass
    // scala.compiletime.error("Copmile Time Error")
    // inline val tBased: T = constValue[T]
    // inline val tName     = tBased.toString
    // scala.compiletime.error(s"Type  ${Type.show[T]} not dealt with")
   */
  /**
    * Makes a Codec for enums (or any T or ElemTypes are Singleton types really) Normal case, enum Foo: A, B, C, DoG object Foo: given
    * Codec[Foo] = enumCaseCodec Need to add an imap to go from DoG => DOG and DOG => DoG for flexibility. This "strict" version allows
    * passing Tuples of case class name to JSON name. This will be "bi-directional. Given ("DoG", "DOG") for enum Foo DOG is written to
    * JSON. When decoding it looks for DOG field and decodes to DoG enum. If no tuple is matching then no mapping. For function based
    * version see enumCaseDynamicCodec. For n Tuples (ai,bi) all a and all b must be unique)
    */
  inline def enumCaseStrictCodec[T](omap: (String, String)*)(using m: Mirror.SumOf[T]): Codec[T] = {

    // From: https://stackoverflow.com/questions/70802124/deserialize-enum-as-string-in-scala-3
    val elemInstances                     = summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[T]]].map(_.value)
    val elemNames                         = summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[String]]].map(_.value)
    val nameToInstanceMap                 = (elemNames zip elemInstances).toMap
    val instanceToNameMap: Map[T, String] = (elemInstances zip elemNames).toMap

    // The CIString should match the Name in the Map no matter what the case.
    val decoder = Decoder[String].emap { name => nameToInstanceMap.get(name).fold(Left(s"Name $name is invalid value"))(Right(_)) }

    // This will always write out the original name of the enum, it will not force to upper, lower, or camel case.
    // Would need to pass in an (optional) function for that.
    val encoder = Encoder[String].contramap[T](instanceToNameMap.apply)

    Codec.from[T](decoder, encoder)
  }
  // Cae Insensitive decoder

  inline def enumCaseCICodec[T](encFn: String => String = _.toUpperCase)(using m: Mirror.SumOf[T]): Codec[T] = {
    val elemInstances =
      summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[T]]].map(_.value).toList
    val elemNames     = summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[String]]].toList

    /** Map of enum "labels" to an value instance of the enum (Singleton enums only of course) */
    val ciNames          = elemNames.map((s: ValueOf[String]) => CIString(s.value))
    val transformedNames = elemNames.map(sv => encFn(sv.value))

    val nameToInstanceMap: Map[CIString, T] = (ciNames zip elemInstances).toMap
    val instanceToNameMap: Map[T, String]   = (elemInstances zip transformedNames).toMap
    val decoder: Decoder[T]                 = Decoder[String].emap { name =>
      nameToInstanceMap.get(CIString(name)).fold(Left(s"Name $name is invalid CaseInsensitive Value: $elemNames"))(Right(_))
    }
    val encoder                             = Encoder[String].contramap[T](instanceToNameMap.apply)
    Codec.from[T](decoder, encoder)
  }

  /**
    * This creates a Codec for a concrete case class (not on the trait) that adds Key Value. So, when *decoding* we can check for the key
    * and get the concrete case class instead of trying each possible sealed child until one fails. BUG: FIXME: This fails compile with an
    * empty case class on summonFrom from within Circe
    */
  inline def deriveCodecWithValue[T](key: String)(d: String)(using m: Mirror.Of[T]): Codec.AsObject[T] = {
    val dec: Decoder[T]          = deriveDecoder[T] // Note: This doesn't care if discriminator is there or not intentionally
    val enc: Encoder.AsObject[T] = deriveEncoder[T].mapJsonObject(jo => jo.add(key, Json.fromString(d)))
    Codec.AsObject.from(dec, enc)
  }

  inline def deriveDiscCodec[T](disc: String = "Foo")(using m: Mirror.Of[T]): Codec.AsObject[T] = {
    // val disc = valueOf[T].getClass.getSimpleName
    deriveCodecWithValue(DiscCodec.discriminatorKey)(disc)
  }
  object DiscCodec:
    final val discriminatorKey = "_I_AM_"

  /** Map is in form  case class field name => JSON field name (eg product -> Product) */

  inline def renamingCodec[T](codec: Codec.AsObject[T], rename: Map[String, String]): Codec.AsObject[T] = Codec
    .AsObject
    .from(
      codec.prepare(prepareKeys(mapKeys(reverse(rename)))),
      codec.mapJsonObject(encoderTransformKey(mapKeys(rename)))
    )

  inline def renamingCodec[T](rename: Map[String, String])(using m: Mirror.Of[T]): Codec.AsObject[T] =
    val codec: Codec.AsObject[T] = deriveCodec[T]
    Codec
      .AsObject
      .from(
        codec.prepare(prepareKeys(mapKeys(reverse(rename)))),
        codec.mapJsonObject(encoderTransformKey(mapKeys(rename)))
      )

  /** Helper for many of opaque type X = String except you need to add ensure to do vaidations on decode. */
  inline def opaqueStringCodec(ensureFn: String => List[String] = _ => List.empty): Codec[String] = Codec
    .from(Decoder.decodeString.ensure(ensureFn), Encoder.encodeString)

}

import scala.quoted.*
private def typeNameImpl[T: Type](using Quotes): Expr[String] = Expr(Type.show[T])

inline def typeName[T]: String = ${ typeNameImpl[T] }

object CirceCodecs extends CirceCodecs

/*

transperant don't really understand.
 */

import scala.compiletime.*
//import scala.inline.*
import scala.deriving.*
inline def fieldNames[T](): List[String] = summonFrom { // Implied T ? Not sure what can be summoned, or is it only Mirrors
  case m: Mirror.ProductOf[T] =>
    scribe.info("ProductOf")
    val mtype = typeName[m.MirroredType]
    scribe.info(s"MirroredType: ${pprint(mtype)}")
    // error("ProductOf") // listeralStrings [m.MirroredElemLabels] ()
    List(mtype)
  case m: Mirror.SumOf[T]     =>
    List("SumOf") // Trait or  enums
    scribe.info("SumOf")
    // val mtype = typeName[m.MirroredType]
    val mtype         = typeName[m.MirroredType]
    val monotype      = typeName[m.MirroredMonoType]
    val elemInstances =
      summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[T]]].map(_.value).toList
    val elemNames     =
      summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[String]]].map(_.value).toList

    scribe.info(s"MirroredType: $mtype  $elemNames $elemInstances $mtype $monotype")
    // error("ProductOf") // listeralStrings [m.MirroredElemLabels] ()
    List(mtype)
  case m: Mirror.Of[T]        => List("Of")
  case x: T                   => List("Error Unmatched codeOf(x)")
}
//
//inline def literalStrings[T](): List[String] =
//  erasedValue[T] match
//    case _: (head *: tail) => constValue[head] :: literalStrings[tail]()
//    case EmptyTuple        => Nil
