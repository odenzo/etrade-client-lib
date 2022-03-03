# e-trade api lib


Scala 3 e-trade web api client


## Tech Stack
- Scala 3.1.1
- Cats / Cats Effect (V3)
- HTTP4S 1.0-Mxx Branch
- FS2
- Circe
- Scribe/SLF4J (WIP)


## Usage 
As a user your main interface point is the OAuth class, and its configuration objects.
After that, you just need to construct your own HTTP4S client (see OAuthClient for examples) and call the API function 
in the package `com.odenzo.etrade.client.api`


## Status

Still very much in-progress, but the basic configuration and OAuth login is done.
A few of the existing APIs are pasted over, but the approach may change for API call based on Scala 3 enhancements.



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

Can also have set it up so you open a browser and login, and paste your verification code into console but took that out.


### Example Program:

In Progress.
Three artifacts are produced, you only need to include  `githubthing %% etrade-client % <verison` it will pull in the Scala oauth and 
models.

While the program itself is backend only, the util and  models will be x-compiled to ScalaJS. 

Better to check back in a few weeks... but for now see MainTest in oauth module tests for the structure.


e-trade oauth proced requires you to login via the webbrowser. On "initial login" thi program will 


