addSbtPlugin("com.eed3si9n"     % "sbt-buildinfo"       % "0.11.0")
addSbtPlugin("com.typesafe.sbt" % "sbt-git"             % "1.0.0")
addSbtPlugin("com.github.sbt"   % "sbt-native-packager" % "1.9.9")
addSbtPlugin("com.codecommit"   % "sbt-github-packages" % "0.5.3")

// Basic ScalaJS Cross-Build
//addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "4.2.4")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.1.0")
addSbtPlugin("ch.epfl.scala"      % "sbt-scalajs-bundler"      % "0.20.0")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"              % "1.9.0")
