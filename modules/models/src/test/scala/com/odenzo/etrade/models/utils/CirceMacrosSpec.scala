package com.odenzo.etrade.models.utils

import com.odenzo.etrade.models.utils.FuzzID.validate
import io.circe.*
import io.circe.generic.semiauto.*
import io.circe.syntax.*
import munit.FunSuite

import scala.runtime.EnumValue

sealed trait FooD
case class FooDA(as: String) extends FooD
case class FooDB(as: Int)    extends FooD

trait UnWrappable[A, B] {
  def unwrap(a: A): B
}

enum Colors derives Codec.AsObject:
  case BLUE, yellow, GReeN

object Colors:
  val bnum: Colors = BLUE

opaque type FuzzID <: String = String

object FuzzID:
  def apply(s: String): FuzzID  = s
  def unwrap(s: FuzzID): String = s

  // Or Cats Validate to a list of error messages (as Strings)
  def validate(s: String): List[String] = {
    List(
      Option.when(s.startsWith("EXP"))("No Experimental Values Allowed"),
      Option.when(s.endsWith("SECURITY"))("SECURTY related values defn can't be used")
    ).flatten
  }

  given Codec[FuzzID] = CirceCodecs.opaqueStringCodec(validate)

opaque type BuzzID = String

object BuzzID:
  def apply(s: String): BuzzID  = s
  def toBase(s: BuzzID): String = s

  given Codec[FuzzID] = CirceCodecs.opaqueStringCodec(validate)

def opaqueDecoder[T, U: Codec]: Decoder[T] = {
  Decoder[U].map((u: U) => asInstanceOf[T])
}

inline def inlineOpaqueDecoder[T, U: Codec]: Decoder[T] = {
  Decoder[U].map((u: U) => asInstanceOf[T])
}

// (using unwrap: UnWrappable[T, U])
def opaqueEncoder[T, U: Encoder]: Encoder[T] = {
  Encoder[U].contramap((t: T) => asInstanceOf[U])
}

class CirceMacrosSpec extends munit.CatsEffectSuite with CirceCodecs {

  test("OT Subtype") {
    val x: FuzzID   = FuzzID("GooGoo")
    val fuzzJson    = x.asJson
    val fuxxBack    = fuzzJson.as[FuzzID]
    scribe.info(s"Fuzz ENcoded: ${fuzzJson.spaces4} $fuxxBack")
    val notGood     = FuzzID("EXPERIMENTAL")
    val notGoodJson = notGood.asJson
    val notGoodBack = notGoodJson.as[FuzzID]
    scribe.info(s"$notGood ENcoded: ${notGoodJson.spaces4} $notGoodBack")

    x.trim
    val y: BuzzID = BuzzID("hello")
    //   y.trim // Will not compile of course.
    // val z: BuzzID = "foo"

  }

  test("Enum") {

//      def printme = {
//        // val rt: EnumValue = new EnumValue[Color]
//        val cc: Array[Colors] = values
//        scribe.info(s"enumValue ${pprint(cc(1))}")
//      }

    // Colors.printme
    val c: Colors = Colors.BLUE

//    scribe.info(s"ColorJSON:  \n ${c.asJson.spaces4}  ")
//    scribe.info(s"ColorKJObj: \n ${c.asJsonObject.asJson.spaces4}  ")
  }

  test("Macros") {
    val a: FooDA = FooDA("ok)")
    val b: FooD  = a
    scribe.info(s"Type Name: ${typeName[FooDA]}")
    scribe.info(s"Field Name: ${typeName[FooDA]}")

  }

  test("MirrorType") {
//    fieldNames[FooD]()   // SumOf Mirror
//    fieldNames[FooDA]()  // Product Mirror
//    fieldNames[Colors]() // MirrorOf Sum
    // fieldNames[BuzzID]() // MirrorOf Sum

  }
}
