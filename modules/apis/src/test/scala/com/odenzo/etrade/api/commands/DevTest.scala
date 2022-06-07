package com.odenzo.etrade.api.commands

import com.odenzo.etrade.models.responses.ListAlertDetailsRs
import io.circe.syntax.{*, given}
class DevTest extends munit.FunSuite {

  val jsonTxt = """{
  "AlertDetailsResponse": {
    "id": 855,
    "createTime": 1654250406,
    "subject": "Funds transfer request received",
    "symbol": "",
    "msgText": "\n        .smart-alert p {\n       ",
    "readTime": 1654565781,
    "deleteTime": 0,
    "next": "https://api.etrade.com/v1/user/alerts/854",
    "prev": "https://api.etrade.com/v1/user/alerts/837"
  }
}"""

//  val json =
//    json"""{
//  "AlertDetailsResponse": {
//    "id": 855,
//    "createTime": 1654250406,
//    "subject": "Funds transfer request received",
//    "symbol": "",
//    "msgText": "\n        .smart-alert p {\n       ",
//    "readTime": 1654565781,
//    "deleteTime": 0,
//    "next": "https://api.etrade.com/v1/user/alerts/854",
//    "prev": "https://api.etrade.com/v1/user/alerts/837"
//  }
//}"""
  test("Bad Codec") {
    val json = io.circe.parser.parse(jsonTxt)
    scribe.info(s"${json.map(_.spaces4)}")
    val o    = json.map(_.as[ListAlertDetailsRs])
    scribe.info(s"${pprint(o)}")
  }
}
