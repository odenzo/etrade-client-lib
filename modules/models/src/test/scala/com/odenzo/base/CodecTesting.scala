package com.odenzo.base

import munit.FunSuite
import io.circe.*

trait CodecTesting extends munit.CatsEffectSuite with CirceUtils {

  def someHelper(): Unit = ()
}
