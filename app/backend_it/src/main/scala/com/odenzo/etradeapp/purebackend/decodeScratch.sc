//import io.circe.syntax.*
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

val json = io.circe.parser.parse(jsonTxt)
