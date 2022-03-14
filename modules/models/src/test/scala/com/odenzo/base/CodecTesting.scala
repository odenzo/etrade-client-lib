package com.odenzo.base

import com.odenzo.etrade.base.CirceUtils
import munit.FunSuite
import io.circe.*

trait CodecTesting extends munit.CatsEffectSuite with CirceUtils {

  def someHelper(): Unit = ()
}
