package cs.dispatch.domain

import cs.dispatch.config.ConfigError
import zhttp.http.HttpError

object HttpErrorResponses {
  def toHttpError(e: Throwable) =
    e match {
      case e: ConfigError => HttpError.InternalServerError(e.getMessage)
      case _ => HttpError.InternalServerError("Unknown server error")
    }
}