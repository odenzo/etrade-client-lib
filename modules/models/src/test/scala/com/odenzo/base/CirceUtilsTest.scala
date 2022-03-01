package com.odenzo.base

import munit.FunSuite
import io.circe.*

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
