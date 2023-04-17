package cs.dispatch.servers.controllers

import cs.dispatch.Context.*
import cs.dispatch.clients.SimpleHttpClient
import zhttp.http.{HttpApp, *}
import zhttp.service.{ChannelFactory, EventLoopGroup}
import zio.*
import zio.UIO
import zio.config.*
import cs.dispatch.Main.validateEnv
import cs.dispatch.config.AppConfig
import cs.dispatch.services.RecommendationService
import cs.dispatch.Main.validateEnv
import cs.dispatch.domain.HttpErrorResponses.toHttpError
import cs.dispatch.domain.{Recommendation, User}
import zio.json.*
import izumi.reflect.dottyreflection.ReflectionUtil.reflectiveUncheckedNonOverloadedSelectable
import cs.dispatch.Main.validateEnv
import magnolia1.Monadic.map

trait RecommendationController {
  def create(): HttpApp[Env & RecommendationService & AppConfig, Throwable]
}

class UserResponseError(msg: String) extends Exception(msg)

case class RecommendationControllerImpl(recommendationService: RecommendationService) extends RecommendationController {
   private type RecommendationServiceEnv = Env & RecommendationService & AppConfig

  private val recommendationsApp: HttpApp[RecommendationServiceEnv, Throwable]  =
    Http.collectZIO[Request] {
      case req@Method.POST -> !! / "creditcards" => {
        (for {
          body <- req.bodyAsString
          user <- ZIO.fromEither(body.fromJson[User])
            .mapError(e => UserResponseError(s"Failed serialize user, UserResponse: $e"))
          data <- recommendationService.getRecommendations(user)
        } yield Response.json(data.toJson))
          .catchAll(e => ZIO.succeed(Response.fromHttpError(toHttpError(e))))
      }
    }

  override def create(): HttpApp[RecommendationServiceEnv, Throwable]  = recommendationsApp
}

object RecommendationController {
  def live: RLayer[RecommendationService, RecommendationController] =
    ZLayer.fromFunction(RecommendationControllerImpl.apply)
}
