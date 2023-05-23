package cs.dispatch.domain

import cs.dispatch.config.ConfigError
import cs.dispatch.controllers.UserResponseError
import zio.http.HttpError

object HttpErrorHandler {
  def toHttpError(e: Throwable) =
    e match {
      case e: UserResponseError => HttpError.BadRequest(e.getMessage)
//      case e: CardResponseError => HttpError.BadRequest(e.getMessage)
//      case e: CreditCardResponseError => HttpError.BadRequest(e.getMessage)
      case e: ConfigError => HttpError.InternalServerError(e.getMessage)
      case _              => HttpError.InternalServerError("Unknown server error")
    }
}