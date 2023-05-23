package cs.dispatch.controllers

import cs.dispatch.clients.SimpleHttpClient
import zio.http.*
import zio.{RLayer, UIO, URLayer, ZIO, ZLayer}
import cs.dispatch.Main.validateEnv
import cs.dispatch.config.{AppConfig, Config}
import cs.dispatch.services.RecommendationService
import cs.dispatch.Main.validateEnv
import zio.json.*
import cs.dispatch.Main.validateEnv
import cs.dispatch.domain.HttpErrorHandler.toHttpError
import cs.dispatch.domain.User
import magnolia1.Monadic.map

trait RecommendationController {
  def create(): App[Any]
}

class UserResponseError(msg: String) extends Exception(msg)

private final case class RecommendationControllerImpl(
    recommendationService: RecommendationService
) extends RecommendationController {
  given encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]

  private val recommendationsApp: App[Any] =
    Http.collectZIO[Request] { case req @ Method.POST -> !! / "creditcards" =>
      (for {
          body <- req.body.asString
          user <- ZIO
            .fromEither(body.fromJson[User])
            .mapError(e =>
              UserResponseError(s"Failed serialize user, UserResponse: $e")
            )
          data <- recommendationService.getRecommendations(user)
        } yield Response.json(data.toJson))
        .catchAll(e => ZIO.succeed(Response.fromHttpError(toHttpError(e))))
    }

  override def create(): App[Any] = recommendationsApp
}

object RecommendationController {
  lazy val live: URLayer[RecommendationService, RecommendationController] =
    ZLayer.fromFunction(RecommendationControllerImpl.apply)
}
