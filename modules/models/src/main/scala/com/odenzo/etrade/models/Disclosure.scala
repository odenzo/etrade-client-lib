package com.odenzo.etrade.models

import io.circe.Codec

/** For orders, may need to make some optional */
case class Disclosure(
    ehDisclosureFlag: Boolean,          //	The disclosure flag value
    ahDisclosureFlag: Boolean,          //	The AH disclosure flag value
    conditionalDisclosureFlag: Boolean, //	The conditional disclosure flag value
    aoDisclosureFlag: Boolean,          //	The advanced order disclosure flag value
    mfFLConsent: Boolean,               //	The mutual fund FL consent flag value
    mfEOConsent: Boolean                //	The mutual fund EO consent flag value

) derives Codec.AsObject
