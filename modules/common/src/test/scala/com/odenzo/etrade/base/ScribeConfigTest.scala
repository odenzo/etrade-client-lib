package com.odenzo.etrade.base

import cats.effect.IO
import com.odenzo.etrade.base.ScribeConfig
import munit.FunSuite
import scribe.Level
class ScribeConfigTest extends FunSuite {
  test("Logging") {
    ScribeConfig.setupRoot(onlyWarnings = List("com.odenzo.base"), initialLevel = Level.Error)
    scribe.error("WEEOE")
    scribe.debug("I Am Debugging")
    scribe.warn("I Am Warning, here me warn")
    scribe.info("I am info")
  }

}

class ScribeIOTest extends munit.CatsEffectSuite {

  test("Logging") {

    val prog = IO.delay {
      scribe.error("ERROR")
      scribe.debug("I Am Debugging")
      scribe.warn("I Am Warning, here me warn")
      scribe.info("I am info")
    }
    ScribeConfig.setupRoot(onlyWarnings = List("com.odenzo.base"), initialLevel = Level.Error)
    prog
  }
}
