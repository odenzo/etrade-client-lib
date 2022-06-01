package com.odenzo.etrade.models.utils

import io.circe.*
import munit.FunSuite

object ElseWhere {

  case class Foo(X: Int, y: Int) derives Codec.AsObject

  object Foo {
    val c = Codec[Foo]

  }
}
class CirceUtilsTest extends CodecTesting {
  import ElseWhere.*
  test("Summon of AutoCodec") {
    scribe.info(s"C = ${Foo.c}")
  }

  test("Renaming") {}
}
