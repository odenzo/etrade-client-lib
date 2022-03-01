package com.odenzo.base

import com.odenzo.base.OPrint.oprint
import scribe.*
import scribe.filter.*
import scribe.format.*
import scribe.filter.FilterMatcher
import scribe.modify.LevelFilter

/**
  * Scribe....I will regret this. Back to blindsight since I can't remember how to config the damn thing. Need to make is sl4j comptable no
  * matter what (scribing or blindsighting), but want to keep ScalaJS comptable too. :-(
  */
object ScribeConfig extends Logger {

  /**
    * @param initialLevel
    *   Initial level for all loggerss, defaults to scribe.Level.Debug
    * @param onlyWarnings
    *   List of package names/prefixes to limit to >= WARN
    * @return
    */
  def setupRoot(initialLevel: Level = Level.Debug, onlyWarnings: List[String] = List("com.odenzo.foo")): Logger = {
    scribe.warn(s"Setting ALL Logging to $initialLevel and muting ${oprint(onlyWarnings)}")

    val myFormatter: Formatter = formatter"->>  [$timeStamp] $levelPaddedRight $position $newLine--   $messages$newLine"

    val filters: FilterBuilder =
      val levelFilter: LevelFilter = scribe.filter.level.<(Level.Warn)
      val startPackages            = onlyWarnings.map(name => PackageNameFilter.startsWith(name))
      select(startPackages*).exclude(levelFilter)

    Logger.root.clearModifiers().clearHandlers()
      .withMinimumLevel(initialLevel)
      .withHandler(formatter = myFormatter)
      .withModifier(filters)
      .replace()
  }
}
