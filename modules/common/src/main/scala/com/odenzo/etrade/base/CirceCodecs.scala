package com.odenzo.etrade.base

object CirceCodecs {
  import io.circe.*

  import scala.compiletime.summonAll
  import scala.deriving.Mirror

  // From: https://stackoverflow.com/questions/70802124/deserialize-enum-as-string-in-scala-3
  inline def stringEnumDecoder[T](using m: Mirror.SumOf[T]): Decoder[T] =
    val elemInstances = summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[T]]].map(_.value)
    val elemNames     = summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[String]]].map(_.value)
    val mapping       = (elemNames zip elemInstances).toMap
    Decoder[String].emap { name => mapping.get(name).fold(Left(s"Name $name is invalid value"))(Right(_)) }

  inline def stringEnumEncoder[T](using m: Mirror.SumOf[T]): Encoder[T] =
    val elemInstances = summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[T]]].map(_.value)
    val elemNames     = summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[String]]].map(_.value)
    val mapping       = (elemInstances zip elemNames).toMap
    Encoder[String].contramap[T](mapping.apply)

  // Cae Insensitive decoder
  inline def stringCIEnumDecoder[T](using m: Mirror.SumOf[T]): Decoder[T] =
    val elemInstances = summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[T]]].map(_.value)
    val elemNames     = summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]]
      .productIterator
      .asInstanceOf[Iterator[ValueOf[String]]]
      .map(_.value.toUpperCase)
    val mapping       = (elemNames zip elemInstances).toMap
    Decoder[String].emap { name => mapping.get(name.toUpperCase).fold(Left(s"Name $name is invalid value"))(Right(_)) }

  /** Exactly the same as StringEnumEncoder for now, doen't update/lowecae original names */
  inline def stringCIEnumEncoder[T](using m: Mirror.SumOf[T]): Encoder[T] =
    val elemInstances = summonAll[Tuple.Map[m.MirroredElemTypes, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[T]]].map(_.value)
    val elemNames     = summonAll[Tuple.Map[m.MirroredElemLabels, ValueOf]].productIterator.asInstanceOf[Iterator[ValueOf[String]]].map(_.value)
    val mapping       = (elemInstances zip elemNames).toMap
    Encoder[String].contramap[T](mapping.apply)

  inline def stringCIEnumCodec[T](using m: Mirror.SumOf[T]): Codec[T] = Codec.from[T](stringCIEnumDecoder, stringCIEnumEncoder)
}
