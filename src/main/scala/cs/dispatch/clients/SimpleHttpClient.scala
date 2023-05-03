package cs.dispatch.clients

import cs.dispatch.config.AppConfig
import zio.*
import zhttp.service.*
import zhttp.*
import zhttp.http.*
import cs.dispatch.Context._

object SimpleHttpClient {
  //val headers = Headers("Content-Type", "application/json")

  def callHttp(
      port: Int,
      host: String,
      path: String,
      method: Method,
      timeout: Duration = 3.seconds,
      body: HttpData = HttpData.fromString("")
  ): ZIO[Env with AppConfig, Throwable, String] =
    for {
      res <- Client
        .request(
          url = s"http://$host:$port$path",
          method = method,
          content = body
        )
        .timeout(timeout)
      data <- res match {
        case Some(r) => r.bodyAsString
        case None =>
          ZIO.logError(
            s"No response from upstream http://$host:$port$path in timely manner."
          ) *> ZIO.succeed("")
      }
    } yield data
}
