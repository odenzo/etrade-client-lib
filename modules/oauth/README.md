# E-Trade API Authorization (Outdated)

OAuth1 style it seems.

Logically:   

1. Get a Consumer Key and Consumer Secret manually for the application.
   Callback must be oob, but a callback may be setup on e-trade.
   (aka ConsumerKey, ConsumerKeySecret)       (Application is "Consumer" in this case.)

2. To authenticate app and user: RequestToken for the app, this returns:
   oauth_token and oauth_token_secret, and then we redirect to etrade login.
   
   This gives the user a verifier code, or does a callback with verifier


3. The Verifier and previous information is used to call getAccessToken which
   gives us the data to do ongoing stuff. (accessToken)


Docs conflicting, Renew Access Token doesn't give a new token, just un-deads old access token.      

N.B. Secrets are used for signing but not sent to E-Trade.
