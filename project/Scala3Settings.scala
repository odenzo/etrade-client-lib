object Scala3Settings {

  val settings = Seq(
    "-source",
    "3.1",
    "-Xmax-inlines",
    "100",
    // "-rewrite",
    "-new-syntax",
    "-language:implicitConversions",
    "-deprecation",     // emit warning and location for usages of deprecated APIs
    "-explain",         // explain errors in more detail
    "-explain-types",   // explain type errors in more detail
    "-feature",         // emit warning and location for usages of features that should be imported explicitly
    "-indent",          // allow significant indentation.
    "-print-lines",     // show source code line numbers.
    "-unchecked",       // enable additional warnings where generated code depends on assumptions
    "-Ykind-projector", // allow `*` as wildcard to be compatible with kind projector
    // "-Xfatal-warnings"  // fail the compilation if there are any warnings
    "-Xmigration"       // warn about constructs whose behavior may have changed since version
  )
}
