package com.odenzo.etrade.oauth.server

import cats.effect.IO
import org.http4s.Uri
import os.CommandResult

/** A function that opens the browser for user login, platform dependant. Should raise error on failure */
opaque type BrowserLaunchFn = Uri => IO[Unit]

/** This is really intended for back-end usage only, but in cross-platform for convenience. See: */
object BrowserLaunchFn:
  val NOOP: BrowserLaunchFn             = (_: Uri) => IO.unit
  val macMicrosoftEdge: BrowserLaunchFn = macOsOpen(Some("Microsoft Edge"))
  val macSafari: BrowserLaunchFn        = macOsOpen(Some("Safari"))
  val macDefault                        = macOsOpen(None)

  /** Validation that URL scheme is defined and HTTP to avoid (some) security issues */
  def ensureHttp(uri: Uri): IO[Unit] =
    val isHTTP = uri.scheme.contains(Uri.Scheme.http) || uri.scheme.contains(Uri.Scheme.https)
    IO.raiseUnless(isHTTP)(Throwable(s"Can only open HTTP URLs not ${uri.scheme}"))

  def macOsOpen(browserName: Option[String]): BrowserLaunchFn = lift((uri: Uri) =>
    ensureHttp(uri) *>
      IO {
        val res: CommandResult =
          browserName match
            case Some(browser) => os.proc("open", "-a", browser, uri.renderString).call()
            case None          => os.proc("open", "-u", uri.renderString).call()

        scribe.info(s"BrowserLaunch[$browserName]: ${pprint(res)}")
      }.void
  )

  def lift(fn: Uri => IO[Unit]): BrowserLaunchFn = fn: BrowserLaunchFn
end BrowserLaunchFn

extension (x: BrowserLaunchFn)
  def open(uri: Uri) = x.apply(uri)
