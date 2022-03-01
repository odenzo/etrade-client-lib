package com.odenzo.etrade.oauth.config

import io.circe.Codec

/** This is an masked version of Consumer */
case class OAuthConsumerKeys(oauthConsumerKey: String, consumerSecret: String) derives Codec.AsObject
