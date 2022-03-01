package com.odenzo.base

import munit.FunSuite

class ScribeLoggingConfigTest extends FunSuite {

  test("Logging") {
    scribe.debug("DEbugging")
    scribe.info("I am logging")
  }

}
