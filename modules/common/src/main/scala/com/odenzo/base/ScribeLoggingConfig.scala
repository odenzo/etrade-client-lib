package com.odenzo.base

import scribe.*
import scribe.filter.*
import scribe.format.*
import scribe.filter.FilterMatcher
import scribe.modify.LevelFilter

/**
  * Scribe has run-time configuration. This is designed to control when developing the codec library and also when using. This is my
  * experiment and learning on how to control The default config fvor scribe is INFO See com.odenzo.ripple.bincodec package information for
  * usage.
  */
object ScribeLoggingConfig extends Logger {

  def setupRoot(): Logger = {

    val myFormatter: Formatter = formatter"->>  [$timeStamp] $levelPaddedRight $position \n--   $messages$newLine"

    val filters: Seq[FilterBuilder] =
      val levelFilter: LevelFilter      = scribe.filter.level.<(Level.Warn)
      val packages: List[FilterBuilder] = List(
        select(PackageNameFilter.startsWith("org,http4s"), PackageNameFilter.startsWith("org,http4s")).exclude(levelFilter)
      )
      packages

    Logger.root.clearModifiers().clearHandlers()
      .withMinimumLevel(Level.Info)
      .withHandler(formatter = myFormatter)
      .withModifier(filters.head)
      .replace()
  }

  /**
    * Helper to filter out messages in the packages given below the given level I am not sure this works with the global scribe object or
    * not. Usage:
    * {{{
    *   scribe.
    * }}}
    * @return
    *   a filter that can be used with .withModifier()
    */
//  def excludePackageSelction(packages: List[String], atOrAboveLevel: Level, priority: Priority): FilterBuilder = {
//    val ps: List[Filter] = packages.map(p => packageName.startsWith(p))
//    val fb               = select(ps: _*).exclude(level < atOrAboveLevel).includeUnselected.copy(priority = priority)
//
//  }

//  def excludeByClass(clazzes: List[Class[_]], minLevel: Level): FilterBuilder = {
//    val names   = clazzes.map(_.getName)
//    val filters = names.map(n => className(n))
//    select(filters: _*).include(level >= minLevel)
//  }
//
//  def setAllToLevel(l: Level): Unit = { scribe.Logger.root.clearHandlers().withHandler(minimumLevel = Some(l)).replace() }
//
//  def addModifiers(packageNames: List[String], l: Level): Unit = {
//    val pri                     = Priority.Normal // unnecessary since clearing existing modifiers, but handy for future.
//    val packages: FilterBuilder = ScribeLoggingConfig.excludePackageSelction(packageNames)
//    scribe.Logger.root.withModifier(packages).replace()
//
//  }

//  def mutePackage(p: String, l: Level = Level.Warn): Unit = {
//    ScribeLoggingConfig.addModifiers(List(p), Level.Warn) // TODO: This really adding, check alter
//  }
//
//  /** Set logging level of all com.odenzo.bincodec.* packages to the level (via filter) Default is for INFO level */
//  def setBinCodecLogging(l: Level): Unit = mutePackage("com.odenzo.bincodec", l)
}
