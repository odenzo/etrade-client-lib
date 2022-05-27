package com.odenzo.etrade.api.models

import com.odenzo.etrade.api.ETradeContext
import org.http4s.Uri
import org.http4s.client.oauth1.Consumer
import org.http4s.syntax.all.*
import io.circe.*

import scala.util.Try
import com.odenzo.etrade.api.models.*
import com.odenzo.etrade.api.models.OAuthConfig

private val encodeUri = Encoder[String].contramap[Uri](uri => uri.renderString)
private val decodeUri = Decoder[String].emapTry[Uri](s => Try(Uri.unsafeFromString(s)))

given uriCodec: Codec[Uri] = Codec.from(decodeUri, encodeUri)

case class ETradeConfig(
    useSandbox: Boolean,
    callback: ETradeCallback,
    localCallback: Option[Uri],
    auth: ETradeAuth,
    apis: ETradeApis = ETradeApis.defaultApis
) derives Codec.AsObject {
  val apiEndpoint: Uri = if useSandbox then apis.sandbox else apis.prod

  def asOAuthConfig: OAuthConfig = OAuthConfig(
    apiEndpoint,
    Consumer(auth.key, auth.secret),
    callback.callback,
    apis.redirect,
    localCallback
  )
  def asContext: ETradeContext   = com.odenzo.etrade.api.ETradeContext(apiEndpoint)

}

/** This is up to you, I tend to leave on this port but you can override. */
case class ETradeCallback(callback: Uri)

object ETradeCallback:
  def default: ETradeCallback              = ETradeCallback(uri"http://localhost:5555/etrade/oauth_callback")
  def localPort(port: Int): ETradeCallback = ETradeCallback(Uri.unsafeFromString("http://localhost:$port/etrade/oauth_callback"))

/** THere are different key/secret pair for Sandbox and Production environment */
case class ETradeAuth(key: String, secret: String)

/** These are fixed values, defaults will be used. */
case class ETradeApis(
    prod: Uri = uri"https://api.etrade.com/",
    sandbox: Uri = uri"https://apisb.etrade.com/",
    redirect: Uri = uri"https://us.etrade.com/e/t/etws/authorize"
)

object ETradeApis:
  def defaultApis: ETradeApis = ETradeApis()
