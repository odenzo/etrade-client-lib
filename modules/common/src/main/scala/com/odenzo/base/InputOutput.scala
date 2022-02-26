package com.odenzo.base
import cats.effect._
import cats.effect.syntax.all._

import cats._
import cats.data._
import cats.syntax.all._

import java.io.{File, FileInputStream, FileOutputStream}

object InputOutput {

  /** Loads text file returnuing list of line content with EOL delimeters */
  def loadTextFile[F[_]: MonadThrow](filename: String)(implicit F: MonadCancel[F, Throwable]): F[List[String]] = {
    import scala.io._
    val acquire: F[BufferedSource]             = F.catchNonFatal(Source.fromFile(filename)(Codec.UTF8))
    // os.read.lines(filename)
    def close(s: Source): F[Unit]              = F.catchNonFatal(s.close())
    implicit val mc: MonadCancel[F, Throwable] = MonadCancel[F, Throwable]
    Resource
      .make(acquire)(close(_))(F)
      .use { src => F.catchNonFatal(src.getLines().toList) }(F)

  }

  def inputStream(f: File): Resource[IO, FileInputStream] = Resource.make {
    IO(new FileInputStream(f)) // build
  } {
    inStream => IO(inStream.close()).handleErrorWith(_ => IO.unit) // release
  }

  /** Resource Manaaged (File) OutputStream */
  def outputStream(f: File): Resource[IO, FileOutputStream] = {
    Resource.make {
      IO(new FileOutputStream(f))
    } {
      outStream => IO(outStream.close()).handleErrorWith(_ => IO.unit) // release
    }
  }
}
