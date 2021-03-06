# e-trade api lib

[![License Apache-2.0](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](https://www.tldrlegal.com/l/apache2)
![Build](https://github.com/odenzo/etrade-client-lib/actions/workflows/ci.yml/badge.svg)
![Branch Push](https://github.com/odenzo/etrade-client-lib/actions/workflows/ci.yml/badge.svg?event=push)
[![contributions welcome](https://img.shields.io/badge/contributions-welcome-brightgreen.svg?style=flat)](https://github.com/dwyl/esta/issues)
[![HitCount](https://hits.dwyl.com/odenzo/etrade-client-lib.svg?style=flat)](http://hits.dwyl.com/odenzo/etrade-client-lib)
![Latest Release](/github/v/release/odenzo/etrade-client-lib?display_name=tag)

This is a simple e-trade oauth client library that I use to query etrade account
for accounting purposes.

Scala3 and Scala JS only.


- Deals with OAuth semi-specifically for e-trade.
- Has a mini-dsl based on Context Functions composed of::
  - XXCmd -- case class with all API request parameters
  - XXRs  -- case class that consumes the JSON response from each API
  - XXCF methods take parameters from (or XXCmd) to convert to canonical Request[IO] (HTTP4S)
  - XXApp methods: XXCmd => XXRs
  - Command Typeclass that adds extension method on XXCmd called "exec()". Concrete types that:
    - Invoke the XXApp methods OR
    - For Front-End, invokes a Bridge function that calls the backend with the command, executes and returns response

- Has ETradeCmd ETradeService[<ResponseType>] Context Functions to inject into XXCF Request building, and XXApp type functions respectively.

This was all just an experiment to use Scala 3, trimmed down the most overboard things and works pretty well.

## Status

+ APIs all implemented and working except OrderAPIs which are half-baked.
  + 
  + e-trade APIs based on request builders
  + Command Pattern - with each command have a type RESULT saying what the expected result type is (psudeo-existential type I guess)
  + e-trade "apps" (aka service) that takes parameters to build a request, and function to return Command.RESULT
  + No Direct Binding of Command implementation to commands.
  + A backend-binding using TypeClasses for back-end calling of e-trade APIs.
  + Soon a front-end binding using TypeClasses that sends (via HTTP) command to backend and executes via backend binding, returning 
    result to front-end.
  + If I still refuse to do productive work try and make a serializable DSL consisting of Command Graphs to send to backend (unlikely!)

### TODOs

- Codec improvements, inline macros or real macros, including for opaque types (enums implemented as function, renaming implementing as 
  inline style macro now.)
- More fine grained typing, most enums handled but a lot more stuff needs opaque types or refined types to disambiguate
- Deal with List[A|B] and A|B CODECs

- Finish off Orders


###  Community effort to fill out the APIs and models objects
The main reason, god its tedious reverse engineering APIs even for simple implementation, especially error handling.

It is nothing special, but it is tiresome making model classes, and finding
all the oddities and documentation gaps. Also as my first real attempt at a JVM and ScalaJS app in Scala 3 maybe its helpful.
I hit plenty of "gotcha" problems.

Would also be good to switch to some Tapir or some generator to enable more re-use without binding to implementation.




Also, I think Scala 3 is great, and Context Functions like ETradeService
and ETradeCall are very useful. Especially in larger programs to
change/expand the injected values without having to update N functions.


## Overview

Scala 3 e-trade web api client. Focussed on Read-Only APIs in Accounts and GetQuote

TLDR:

Scala Project Dependency:
`libraryDependencies += "com.odenzo" %%% "etrade-client-api" % "0.0.4")`
`libraryDependencies += "com.odenzo" %% "etrade-client-server" % "0.0.4")`

+ Go to E-Trade and Generate an Indivual User Consumer Key and Secret for "live" and "sandbox" trading.

Probably better to see the etrade-client-demo-app

To use the library you must first made an `OAuthConfig` instance, a crude example:

```scala3
 def createConfig(args: List[String], useSandbox: Boolean = false): IO[OAuthConfig] = IO {
    val url: Uri    = uri"https://api.etrade.com/"
    val sb: Uri     = uri"https://apisb.etrade.com/"
    val callbackUrl = uri"http://localhost:5555/etrade/oauth_callback"
    val redirectUrl = uri"https://us.etrade.com/e/t/etws/authorize"

    val sbKey    = scala.sys.env("ETRADE_SANDBOX_KEY")
    val sbSecret = scala.sys.env("ETRADE_SANDBOX_SECRET")
    val key      = scala.sys.env("ETRADE_LIVE_KEY")
    val secret   = scala.sys.env("ETRADE_LIVE_SECRET")

    if !useSandbox
    then com.odenzo.etrade.internal.OAuthConfig(oauthUrl = url, apiUrl = url, consumer = Consumer(key, secret), callbackUrl, redirectUrl)
    else com.odenzo.etrade.internal.OAuthConfig(oauthUrl = url, apiUrl = sb, consumer = Consumer(sbKey, sbSecret), callbackUrl, redirectUrl)
  }
```
Generally you want to be *very* careful with the Consumer keys.
SOPS or some other encryption mechanism. No we want 
to actually make some API call, its a two phase setup, first you 
have to login via a web browser.

NOTE: Hardwired to Safari on MacOS still.

This begined with oauth.login() which will start a local webserver,
and open a browser to etrade where you login with your account credentials.

When that completed, we have the oauth access token, so we shutdown the WebServer.

The access token has a TTL of about 2 hours from last usage, and gets invalidated
at Midnight New York time.

```scala
  def run(config: OAuthConfig) = {
  val oauth   = OAuth(config)                //  setting up
  val context = ETradeContext(config.apiUrl) // Context with info needed to construct HTTP Requests
  for {
    login <- oauth.login()
    res   <- OAuthClient.signingDebugClient(login).use(cio => BusinessMain.run(cio, context))
  } yield res
}
```
There is some flexibility in the HTTP4S client, but you can use the "default" setup
which logs (security risk!) to stdout and automatically signs requests.

So, this is all pretty much boilerplate to get rolling. 
To actually use the APIs, the easiest method is to use the built-in "services".
For example:

```scala
  def account(): ETradeService[Option[Account]] = {
    for {
      myAccount    <- Services.listAccountsApp().map(_.accounts.headOption) 
      _ = scribe.info(s"Account: ${oprint(myAccount)}")
    } yield myAccount

  }
```
The "trick" is that ETradeService is a new Scala 3 Context function.
```scala
type ETradeService[T] = (ETradeContext, Client[IO]) ?=> IO[T]
```
There is something similar used in defining the API requests, but without 
requiring the Client[IO]. Note you can make your own Client[IO] but be sure
to include OAuth signing middleware.


## Implemented (Fully or Partially - some options stuff missing especially)
* Accounts
  * listAccounts
  * accountBalances
  * listTransactions
  * transactionDetail
  * viewPortfolio

* Market
  * GetQuotes
  * LookupProduct
 
## Tech Stack
- Scala 3.1.1
- Cats / Cats Effect (V3)
- HTTP4S 1.0-Mxx Branch  - Ember Client, since speed isn't an issue and ScalaJS compatable
- FS2
- Circe
- Scribe/SLF4J 

No `F[_:Sync]` (tagless) stuff, your stuck with IO for the majority of stuff.


## Usage 
As a user your main interface point is the OAuth class, and its configuration objects.
After that, you just need to construct your own HTTP4S client (see OAuthClient for examples) and call the API function 
in the package `com.odenzo.etrade.client.api`


## Status

+ Workable for the APIs I am using, which center on reporting for Mutual Funds and Equities. Not much work on Options


## Details

### e-trade consumer keyss
To access the e-trade APIs you need an account on e-trade, and then you have to apply for "personal" OAuth Consumer key.
You get a pair, one for the "live" account and one for the "sandbox" environment.
You can configure OAuthConfig to use either of these.

### OAuth Callback
This program works in the OAuth callback mode, meaning on e-trade you specify a callback like 'http://localhost:5555/etrade/oauth'
This program will open a webbrowser application (e.g. Safari/Edge/Chrome) to the URL, the user enters their etrade user/password
and the browser redirects to the callback.

The callback is configurable.

TODO: The command line argument to open the WebBrowser is not configurable yet.

Can also have set it up for your  open a browser and login, and paste your verification code into console but took that out.


### Example Program:

Check out the repo and look at example module. 
Be aware it has a hack so I can login once and re-run the program multiple times.


# ETrade API Oddities Not Handled

## Error Handling Related
### AccountsBalance
Even with Accept(application/json) this will return XML on bad arguments with error message (HTTP Sttaus 400 at least!)
Fix: expect(X).onError(expect(Y)) style and throw an error with Y in it.



### oauth/request token
This is acting up, and often redirecting me to the etrade home page. Running again it works. This is out of regular etrade hours
so not sure what is up.


## TODO List:

+ Configuration of browser open
+ ScalaJS for Models at least
+ Make it totally runnable from the brower and redirect in browser (?)
+ ScalaDoc 3 tuning and microsite generation?