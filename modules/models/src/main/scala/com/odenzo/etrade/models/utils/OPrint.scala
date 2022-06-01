package com.odenzo.etrade.models.utils

import io.circe.{Json, JsonObject}
import pprint.{PPrinter, Tree}
import io.circe.syntax.*
import cats.syntax.all.*

object OPrint {
  def oprint[A](a: A): String = pp.apply(a, 120, 10000).render

  def secretHandler(a: Any): Option[Tree] =
    a match {
      case a: Secret     => pprint.Tree.Literal(f"Secret: ${a.toString}").some
      case a: JsonObject => pprint.Tree.Literal(f"JsonObject: ${a.asJson.spaces4}").some
      case a: Json       => pprint.Tree.Literal(f"Json: ${a.asJson.spaces4}").some
      case _             => Option.empty[Tree]
    }

  val pp =
    new PPrinter(
      defaultWidth = 100, // Because often after logback prefix
      defaultHeight = 1000,
      defaultIndent = 2,
      additionalHandlers = (secretHandler _).unlift,
      colorLiteral = fansi.Color.Yellow ++ fansi.Bold.On,
      colorApplyPrefix = fansi.Color.Magenta ++ fansi.Bold.On
    )
}
