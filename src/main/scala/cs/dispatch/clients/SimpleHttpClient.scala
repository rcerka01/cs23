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
       body: HttpData): ZIO[Env with AppConfig, Throwable, String] =
    for {
      res <- Client.request(
        url = s"http://$host:$port$path",
        method = method,
        content = body
      )
      data <- res.bodyAsString
  } yield data
}
