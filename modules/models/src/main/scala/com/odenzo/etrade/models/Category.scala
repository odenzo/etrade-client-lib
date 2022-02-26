package com.odenzo.etrade.models

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

/** E-Treade Catetory */
case class Category(categoryId: String, parentId: String, categoryName: String, parentName: String)

object Category {
  implicit val codec: Codec.AsObject[Category] = deriveCodec[Category]
}
