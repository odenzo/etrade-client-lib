# Client Usage

This is a custom OAuth to deal with e-trade APIs.
Its pretty much OAuth 1.
Request Tokens have TTL around 5-10 minutes
Access Tokens having a couple hours TTL, and can be refreshed.
At End-of-Day New York time they are cancelled and a new login must be done.

Login is always manual with the pseudo-flow:

1. Get Request Token for Sandbox or Prod (using Consumer Keys)
2. With request token, open e-trade login URL in browser, user does a few steps to login
3. The account MUST be setup, and the setup defined an OAuth callback or no callback. I always use callback,
4. The callback receives Auth and Verify values, which are then exhanged for access token.

If no callback is used, then cut-paste of the VERIFIER token to use API to fetch access token.

These library can be used in a "total backend" mode, in which case it pops open a browser window to redirect URL, and runs a WebServer that
handles the callback URL.

If used in a front-end/back-end way, it is designed to work with just one user now, basically the browser as a UI style.
You could do API calls to e-trade from front-end ScalaJS but CORS prevents this.

So, the approach then would be to have a long-running back-end server and initiate the login from the front-end redirect.
There are lots of approaches so not much direct support is provided here.

The focus is on executing e-trade API via "Command Approach".


## Authentication via E-Trade Callback

In this case a browser window is opened and the user logs in on the e-trade authentication page, says OK to a few
e-trade prompts, and then a callback is made to a webserver running on localhost (at the provided URL).

This of course requires the JVM to run the web-server, or I don't know how to run one in a browser yet listening to a set port.
You can run this when you want a scala backend that also services the ScalaJS front-end, or a pure backend-application (CLI etc)

This is the preferred way to run it, or the way I usually do at least.

```scala
    // Given the above configurations are set
    val oauthConfig = ???
    val context = ???
    val oauth:OAuthCallback  = OAuthCallback(oauthConfig)

    // Invoking login opens the web browser (and starts the web server). It waits until user logs-in with a timeout.
    val login: IO[AuthSessionData] = oauth.login()
    login.flatmap(authSession =>  OAuthClient.signingDebugClient(authSession).use{ client => 
        // Your business code here.
    }

```
This is structured oddly and may be rewritten. The login is not done as resource initialization because
it has required human intervention. The session would normally stayed logged in a few hours.

The HTTP4S client is a resource, but remember this is a pooled resource so is can be used in multiple threads.
It is reasonable to make one client for your whole program and use it in multi-threaded cases.


## Authentication via E-Trade "Magic Word"

In this case the user logs into an etrade authentication page, but with no callback. After logging in they are given
a verification word which they need to cut and paste into your application webpage and it is used to do some calls to fully
authorize the user. Then a HTTP4S client with auatomatic API call signing is produced.

```scala

```