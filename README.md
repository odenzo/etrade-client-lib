# e-trade api lib


Scala 3 e-trade web api client (Being transition from Scala 2.xx now so a bit messy)

Cats/HTTP4S Based
Does OAuth1 via running a dummy callback server and opening a browser to allow user to enter username/password.

Partial, and mainly to deal with my brokerage account monitoring and taxation issues at this point. Not facilities for trading.
See IBKR-TWS-lib if thats important.

This is a pure backend package now, but will transition to ScalaJS models and the library usable from WebApp calling backend APIs.




