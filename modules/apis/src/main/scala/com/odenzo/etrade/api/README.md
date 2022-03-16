# Client Usage

Configure the case class ETradeConfig from its components and your values.

The e-trade client can be run solely from the browser, or with a JVM backend and (optional) front end components.

The primary/only real different is how the login is accomplished. Once authaorized e-trade calls are made the same way.
An HTTP4S client is provided with middle-ware that automatically handled the authentication for API calls once logged in.
So, you just choose one of the two ways to get the initial authentication:

For both methods you first have to setup the configuration, if not using callback just make something up:

```scala
    import com.odenzo.etrade.api.engine.ETradeContext
import com.odenzo.etrade.client.models.OAuthConfig

val config = ETradeConfig(useSandbox = true, callback = ???, auth = ???)
val oauthConfig: OAuthConfig = config.asOAuthConfig()
val context: ETradeContext = config.asContext()

```


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