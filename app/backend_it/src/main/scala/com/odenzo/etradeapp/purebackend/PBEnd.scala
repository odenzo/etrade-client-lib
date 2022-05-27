package com.odenzo.etradeapp.purebackend

import cats.effect.*
import cats.effect.syntax.all.*
import cats.*
import cats.data.*
import cats.syntax.all.*
import com.odenzo.etrade.api.ETradeContext
import com.odenzo.etrade.api.commands.{CommandExecutor, ListAccounts}
import com.odenzo.etrade.api.models.{ETradeApis, ETradeAuth, ETradeCallback, ETradeConfig, OAuthConfig}
import com.odenzo.etrade.models.responses.ListAccountsRs
import com.odenzo.etrade.oauth.OAuthClientMiddleware
import com.odenzo.etrade.oauth.server.*
import com.odenzo.etradeapp.investing.SimpleProg
import org.http4s.client.Client

/** A Pure Backend App that uses etrade-client-lib to do login (and popup browser) Just a quick have to demonstrate logging in and pulling
  * some info. This has the default mode to load an access token from disk.
  * Check if it works, if not, create a new one and override the one on disk. Meh.... some autologin browser scraping thing better
  * but I am sure its not straight forward.
  */
object PBEnd extends IOApp {

  def run(args: List[String]) = {
    scribe.info("PURE BACKEND RUNNING")

    lazy val authEnv: ETradeAuth = ETradeAuth(scala.sys.env("ETRADE_LIVE_KEY"), scala.sys.env("ETRADE_LIVE_SECRET"))
    val etradeConfig             = ETradeConfig(false, ETradeCallback.default, localCallback = None, auth = authEnv, ETradeApis.defaultApis)
    val config                   = etradeConfig.asOAuthConfig
    val browserOpen              = com.odenzo.etrade.oauth.server.BrowserLaunchFn.macMicrosoftEdge
    WebFactory.baseClientR[IO].use { baseClient =>
      for {
        client  <- OAuthClientMiddleware.wrapLogger(true)(baseClient)
        oauth   <- OAuthFramework(config)
        _        = scribe.info(s"Using Pure Backend Framework w/ Config: ${pprint(config)}")
        session <- oauth.backendBrowserLauncherKilling(browserOpen)(using client)
        _       <- session match {
                     case Left(err)      => IO.raiseError(err)
                     case Right(session) =>
                       val context                           = new ETradeContext(config.apiUrl)
                       val clientR: Resource[IO, Client[IO]] = oauth.constructSigningMiddlewareClient(session)
                       val resource                          = clientR.map(c => (c, context))
                       scribe.info("Authenticated and Running Business Logic")
                       business(resource)

                   }
      } yield ExitCode.Success
    }
  }

  def business(resource: Resource[IO, (Client[IO], ETradeContext)]) = {
    resource.use { (client, context) =>
      val etrade          = new CommandExecutor(using context, client)
      given ETradeContext = context
      given Client[IO]    = client

      val executor: IO[Unit] =
        etrade.fire(ListAccounts()).flatMap {
          case rq: ListAccountsRs => IO(scribe.info(s"My Accounts: $rq "))
          case other              => IO(scribe.info(s"Unexpected Response: $other"))

        }

      val summoned: IO[Unit] = {
        import com.odenzo.etrade.api.commands.Executable
        import com.odenzo.etrade.api.commands.given
        val M                      = summon[Executable[ListAccounts, ListAccountsRs]]
        val rs: IO[ListAccountsRs] = ListAccounts().exec()
        rs.void
      }
      executor *> summoned
    }
  }




  def readStoredToken() = os.
}
