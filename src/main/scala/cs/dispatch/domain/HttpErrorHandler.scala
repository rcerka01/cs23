package cs.dispatch.domain

import cs.dispatch.config.ConfigError
import cs.dispatch.controllers.UserResponseError
import zio.http.{HttpError, Response}
import sttp.model
import sttp.model.StatusCode

case class ErrorResponse(
    status: sttp.model.StatusCode,
    errorResponse: HttpError
)

object HttpErrorHandler {
  def toHttpError(e: Throwable): ErrorResponse =
    e match {
      case e: UserResponseError =>
        ErrorResponse(
          sttp.model.StatusCode.BadRequest,
          HttpError.BadRequest(e.getMessage)
        )
      case e: ConfigError =>
        ErrorResponse(
          sttp.model.StatusCode.InternalServerError,
          HttpError.InternalServerError(e.getMessage)
        )
      case _ =>
        ErrorResponse(
          sttp.model.StatusCode.InternalServerError,
          HttpError.InternalServerError("Unknown server error")
        )
      // no need as requested as 200 + None
      // case e: CardResponseError =>
      // case e: CreditCardResponseError =>
    }
}
