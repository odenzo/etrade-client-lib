package com.odenzo.etrade.oauth.client

import cats.effect.IO
import com.odenzo.etrade.oauth.client.ClientFactory // JS /JVM Split
object ClientFactoryProxy {

  def getClientResource = ClientFactory.baseClientR[IO]() // TODO: Wrap in middleware as needed
}
