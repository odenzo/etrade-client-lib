package com.odenzo.etrade.models

import com.odenzo.etrade.models.utils.CirceCodecs
import io.circe.Codec

opaque type AccountId <: String = String

object AccountId:
  // This could be a macro? On assumption validated is always there, or could generate some predef validations?
  // https://msitko.pl/blog/build-your-own-refinement-types-in-scala3.html
  def apply(s: String): AccountId =
    validated(s) match {
      case Nil    => s
      case errors => throw IllegalArgumentException(s"$s invalid: ${errors.mkString("::")}")
    }

  def validated(s: String): List[String] = if s.isBlank then List.empty[String] else List("AccountID Cannot be blank")
  given Codec[AccountId]                 = CirceCodecs.opaqueStringCodec(validated)

opaque type AccountIdKey <: String = String

object AccountIdKey:
  // This could be a macro? On assumption validated is always there, or could generate some predef validations?
  // https://msitko.pl/blog/build-your-own-refinement-types-in-scala3.html
  def apply(s: String): AccountIdKey =
    validated(s) match {
      case Nil    => s
      case errors => throw IllegalArgumentException(s"$s invalid: ${errors.mkString("::")}")
    }

  def validated(s: String): List[String] = if s.isBlank then List.empty[String] else List("AccountID Cannot be blank")
  given Codec[AccountIdKey]              = CirceCodecs.opaqueStringCodec(validated)
