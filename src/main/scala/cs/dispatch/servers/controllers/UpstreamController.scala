package cs.dispatch.servers.controllers

import cs.dispatch.clients.SimpleHttpClient
import zio.config.ReadError
import zio.http.{HttpApp, *}
import zio.{RLayer, UIO, ZIO, ZLayer}
import cs.dispatch.Main.validateEnv
import cs.dispatch.config.{AppConfig, ConfigError}
import cs.dispatch.services.{CallType, UpstreamImitatorService}
import cs.dispatch.config.*
import cs.dispatch.domain.HttpErrorResponses.toHttpError
import zio.http.Status.BadRequest

trait UpstreamController {
  def create(): App[Any]
}

case class UpstreamControllerImpl(upstreamService: UpstreamImitatorService)
    extends UpstreamController {

  private val upstreamApp: App[Any] =
    Http.collectZIO[Request] {
      case req @ Method.POST -> !! / "test" =>
          req.body.asString.mapBoth(_ => Response.status(BadRequest), Response.json)
      case req @ Method.GET -> !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v1" / "cards" =>
        upstreamService
          .cardImitator(CallType.Cards)
          .provide(Config.live)
          .fold(e => Response.fromHttpError(toHttpError(e)), Response.json)
      case req @ Method.GET -> !! / "app.clearscore.com" / "api" / "global" / "backend-tech-test" / "v2" / "creditcards" =>
        upstreamService
          .cardImitator(CallType.CreditCards)
          .provide(Config.live)
          .fold(e => Response.fromHttpError(toHttpError(e)), Response.json)
    }

  override def create(): App[Any] = upstreamApp
}
object UpstreamController {
  def live: RLayer[UpstreamImitatorService, UpstreamController] =
    ZLayer.fromFunction(UpstreamControllerImpl.apply)
}
