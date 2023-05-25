package cs.dispatch.controllers

import cs.dispatch.clients.SimpleHttpClient
import zio.http.{Response, *}
import zio.{RLayer, UIO, URLayer, ZIO, ZLayer}
import cs.dispatch.Main.validateEnv
import cs.dispatch.config.{AppConfig, Config}
import cs.dispatch.services.RecommendationService
import cs.dispatch.Main.validateEnv
import zio.json.*
import cs.dispatch.Main.validateEnv
import cs.dispatch.domain.HttpErrorHandler.toHttpError
import cs.dispatch.domain.{Recommendation, User}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import magnolia1.Monadic.map
import sttp.model.StatusCode
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.PublicEndpoint
import sttp.tapir.ztapir._
import sttp.tapir.server.ziohttp.ZioHttpInterpreter
import zio.http.{HttpApp, Request, Response}
import zio._
import sttp.tapir.generic.auto._

trait RecommendationController {
  def create(): App[Any]
}

class UserResponseError(msg: String) extends Exception(msg)

private final case class RecommendationControllerImpl(
    recommendationService: RecommendationService
) extends RecommendationController {

  val recommendationsEndpoint =
    sttp.tapir.ztapir.endpoint.post
      .in("creditcards")
      .errorOut(statusCode)
      .in(jsonBody[User])
      .out(jsonBody[List[Recommendation]])
      .description("""
          |single endpoint that consumes some
          |information about the user’s financial situation and return credit cards
          |recommended for them
          |""".stripMargin)

  private val recommendationsTapirApp: App[Any] =
    ZioHttpInterpreter()
      .toHttp(recommendationsEndpoint.zServerLogic { user =>
        recommendationService
          .getRecommendations(user)
          .mapError((e: Throwable) => toHttpError(e).status)
      })
      .mapError(e => Response.fromHttpError(toHttpError(e).errorResponse))

  ///////////////////////////////////////////////////////////////////////////////////////////
  //  private val recommendationsApp: App[Any] =
  //    Http.collectZIO[Request] { case req @ Method.POST -> !! / "creditcards2" =>
  //      (for {
  //          body <- req.body.asString
  //          user <- ZIO
  //            .fromEither(body.fromJson[User])
  //            .mapError(e =>
  //              UserResponseError(s"Failed serialize user, UserResponse: $e")
  //            )
  //          data <- recommendationService.getRecommendations(user)
  //        } yield Response.json(data.toJson))
  //        .catchAll(e => ZIO.succeed(Response.fromHttpError(toHttpError(e))))
  //    }

  override def create(): App[Any] =
    recommendationsTapirApp //++ recommendationsApp ++ countCharactersHttp
}

object RecommendationController {
  lazy val live: URLayer[RecommendationService, RecommendationController] =
    ZLayer.fromFunction(RecommendationControllerImpl.apply)
}
