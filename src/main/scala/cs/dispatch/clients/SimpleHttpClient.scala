package cs.dispatch.clients

import cs.dispatch.config.AppConfig
import zio.*
import zio.http.*

object SimpleHttpClient {

  def callHttp(
      port: Int,
      host: String,
      path: String,
      method: Method,
      timeout: Duration = 3.seconds,
      body: Body = Body.fromString("")
  ) =
    for {
      res <- Client
        .request(
          url = s"http://$host:$port$path",
          method = method,
          content = body
        )
        .timeout(timeout)
      data <- res match {
        case Some(r) => r.body.asString
        case None =>
          ZIO.logError(
            s"No response from upstream http://$host:$port$path in timely manner."
          ) *> ZIO.succeed("")
      }
    } yield data
}
