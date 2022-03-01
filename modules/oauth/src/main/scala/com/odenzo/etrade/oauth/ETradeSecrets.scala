package com.odenzo.etrade.oauth

import com.odenzo.etrade.oauth.config.OAuthConsumerKeys
import io.circe.Codec
import io.circe.generic.AutoDerivation

case class ETradeSecrets(sandbox: OAuthConsumerKeys, prod: OAuthConsumerKeys) extends AutoDerivation
